package it.apeiron.pitagora.core.service.auth;

import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.SUPER_ADMIN;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.apeiron.pitagora.core.config.auth.JwtAuthenticationToken;
import it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.util.Language;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class JwtService {

	private ServiceProvider sp;
	public void setSp(ServiceProvider sp) {
		this.sp = sp;
		_init();
	}

	public static final long JWT_TOKEN_VALIDITY = 2 * 60 * 60;
	public static final String ROLES_CLAIM = "ROLES";

	private final Set<String> notValid = new HashSet<>();

	private final ObjectMapper om = new ObjectMapper();
	@Value("${jwt.secret}")
	private String secret;

	private void _init() {
		Language.JWT_SERVICE = this;
	}

//	public static String t(MessagesCore... msgs) {
//		return (THIS.getLoggedUser().getLanguage() != null ? THIS.getLoggedUser().getLanguage() : Language.IT).t(msgs);
//	}

	public String generateToken(PitagoraUser pitagoraUser) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(ROLES_CLAIM, Arrays.asList(pitagoraUser.getRole().getAuthorities()));
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(pitagoraUser.getEmail())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public PitagoraUser validateToken(HttpServletRequest request) {
		try {
			String token = _getTokenFromRequest(request);
			String username = _getUsernameFromToken(token);
			Claims map = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
			List<String> tokenAuthorities = (ArrayList<String>) map.get(ROLES_CLAIM);

			PitagoraUser user = _loadUserByUsername(username);
			if (!notValid.contains(token) &&
					Arrays.asList(user.getRole().getAuthorities()).containsAll(tokenAuthorities) &&
					!_isTokenExpired(token)) {
				return user;
			}

		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		return null;
	}

	public void invalidateToken(HttpServletRequest request) {
		notValid.add(_getTokenFromRequest(request));
	}

	public boolean isSuperAdmin() {
		return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
				.getAuthorities()
				.contains(new SimpleGrantedAuthority(SUPER_ADMIN.name()));
	}

	private PitagoraUser _loadUserByUsername(String email) throws UsernameNotFoundException {
		return sp.userService.findByEmail(email);
	}

	private String _getUsernameFromToken(String token) {
		return _getClaimFromToken(token, Claims::getSubject);
	}

	private Date _getExpirationDateFromToken(String token) {
		return _getClaimFromToken(token, Claims::getExpiration);
	}
	private <T> T _getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = _getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims _getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	private Boolean _isTokenExpired(String token) {
		final Date expiration = _getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private String _getTokenFromRequest(HttpServletRequest request) {
		return request.getHeader("Authorization");
//		!= null ?
//				Arrays.stream(request.getCookies()).filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
//				.map(Cookie::getValue).findFirst()
//				.orElse(null) : null;
	}

	public PitagoraUser getLoggedUser() {
		return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
	}
	public Language getLoggedUserLanguage() {
		return getLoggedUser().getLanguage() != null ? getLoggedUser().getLanguage() : Language.IT;
	}

	public boolean userHasRightsOn(AbstractPitagoraRecord pit) {
		return getLoggedUser().getTheorems().containsAll(pit.getScope().stream().filter(scope -> !Theorem.ANALYSIS_TOOLS.equals(scope)).collect(
				Collectors.toList()));
	}

	public Object getUserTheoremPreferences(Theorem thm) {
		AtomicBoolean mustSave = new AtomicBoolean(false);
		if (getLoggedUser().getPreferences() == null) {
			getLoggedUser().setPreferences(new HashMap<>());
			mustSave.set(true);
		}

		if (!getLoggedUser().getPreferences().containsKey(thm)) {
			getLoggedUser().getPreferences().put(thm, thm.getUserPreferences());
			mustSave.set(true);
		}
		Map<String, Object> defaultPrefs = om.convertValue(thm.getUserPreferences(), Map.class);
		Map<String, Object> currentPrefs = om.convertValue(getLoggedUser().getPreferences().get(thm), Map.class);
		defaultPrefs.forEach((k, v) -> {
			if (!currentPrefs.containsKey(k) || currentPrefs.get(k) == null) {
				currentPrefs.put(k, defaultPrefs.get(k));
				mustSave.set(true);
			}
		});

		if (mustSave.get()) {
			getLoggedUser().getPreferences().put(thm, om.convertValue(currentPrefs, thm.getUserPreferences().getClass()));
			sp.mongoTemplate.save(getLoggedUser());
		}
		return getLoggedUser().getPreferences().get(thm);
	}
}
