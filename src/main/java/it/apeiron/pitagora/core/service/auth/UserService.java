package it.apeiron.pitagora.core.service.auth;

import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.ADMIN;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.ANALYST;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.SUPER_ADMIN;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.USER;

import it.apeiron.pitagora.core.dto.ChangePasswordDTO;
import it.apeiron.pitagora.core.dto.UserDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraUserRepository;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.util.Language;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class UserService {

    public final static String FAKE_MAIL = "nome.cognome@apeironspa.it";

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PitagoraUserRepository userRepository;

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    @PostConstruct
    @Transactional
    public void init() {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            return;
        }
        userRepository.save(PitagoraUser.builder()
                .email(UserService.FAKE_MAIL)
                .language(Language.IT)
                .password(passwordEncoder.encode("superadmin"))
                .firstName("Nome")
                .lastName("Cognome")
                .company("Azienda")
                .phone("12345")
                .role(Role.SUPER_ADMIN)
                .theorems(Arrays.asList(Theorem.values()))
                .build());
    }

    public PitagoraUser loadUserByUsernameAndValidatePassword(String username, String password) throws UsernameNotFoundException {
        PitagoraUser user = _loadUserByUsername(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }

        return user;
    }

    private PitagoraUser _loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findPitagoraUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' not found"));
    }

    public boolean checkEmailExistence(String email) {
        return userRepository.existsByEmail(email) && !email.equals(jwtService.getLoggedUser().getEmail());
    }

    @Transactional
    public void create(UserDTO userDto) {
        if (checkEmailExistence(userDto.getEmail())) {
            throw PitagoraException.notAcceptable("This email address is already being used");
        }
        Role roleToCreate = Role.valueOf(userDto.getRole().getValue());
        if (Role.SUPER_ADMIN.equals(roleToCreate) || (ADMIN.equals(roleToCreate) && !jwtService.isSuperAdmin())) {
            throw PitagoraException.forbidden("Creation of " + roleToCreate.name() + " Forbidden");
        }
        PitagoraUser user = PitagoraUser.fromDto(userDto);
        user.setId(null);
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        log.info("User successfully created");
    }

    @Transactional
    public void update(UserDTO userDto) {
        PitagoraUser user = userRepository.findById(new ObjectId(userDto.getId()))
                .orElseThrow(() -> PitagoraException.notAcceptable("User with id '" + userDto.getId() + "' not found"));
        Role roleToSet = Role.valueOf(userDto.getRole().getValue());
        boolean someoneIsCreatingSuperAdmin = SUPER_ADMIN.equals(roleToSet) && !SUPER_ADMIN.equals(user.getRole());
        boolean otherThanSuperAdminIsCreatingAdmin = ADMIN.equals(roleToSet) && !ADMIN.equals(user.getRole()) && !jwtService.isSuperAdmin();

        if (someoneIsCreatingSuperAdmin || otherThanSuperAdminIsCreatingAdmin) {
            log.error("Role change forbidden");
            throw PitagoraException.forbidden("Cambio Ruolo non permesso");
        }
        if (StringUtils.isNotEmpty(userDto.getPassword().trim())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword().trim()));
        }
        if (!userDto.getEmail().trim().equals(user.getEmail())) {
            if (!jwtService.isSuperAdmin() || user.getEmail().equals(FAKE_MAIL)) {
                user.setEmail(userDto.getEmail().trim());
            } else {
                log.error("Email change forbidden");
                throw PitagoraException.forbidden("Cambio Email non permesso");
            }
        }
        user.update(userDto);
        userRepository.save(user);
        log.info("User successfully updated");
    }

    public List<ValueDescriptionDTO> findAll(String query) {
        boolean isSuperAdmin = jwtService.isSuperAdmin();
        String regex = "(?i).*" + query + ".*";
        return userRepository.findByExample(regex).stream()
                .filter(user -> !jwtService.getLoggedUser().getId().equals(user.getId())
                        && (isSuperAdmin || ANALYST.equals(user.getRole()) || USER.equals(user.getRole())))
                .map(user -> new ValueDescriptionDTO(user.getId(), user.getFirstName() + " " + user.getLastName() + "\t" + user.getRole().getDescription())).collect(Collectors.toList());
    }

    public UserDTO findById(ObjectId userId) {
        return UserDTO.fromModel(userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id'" + userId + "' not found")));
    }

    @Transactional
    public void delete(ObjectId userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        PitagoraUser logged = jwtService.getLoggedUser();
        if (!passwordEncoder.matches(dto.getOldPassword(), logged.getPassword())) {
            throw PitagoraException.badRequest("La vecchia password è errata");
        } else if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw PitagoraException.badRequest("La conferma della nuova password non corrisponde");
        }
        logged.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(logged);
        log.info("Password successfully updated");
    }

    public PitagoraUser findReceiverUser() {
        return this.userRepository.findByRole(SUPER_ADMIN).get();
    }

    public boolean userExistsByEmail(String username) {
        return userRepository.existsByEmail(username);
    }

    public PitagoraUser findByEmail(String email) {
        return userRepository.findPitagoraUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' not found"));
    }
}
