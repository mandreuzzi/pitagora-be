package it.apeiron.pitagora.core.config.auth;

import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final PitagoraUser user;

    public JwtAuthenticationToken(PitagoraUser user,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public PitagoraUser getPrincipal() {
        return user;
    }
}
