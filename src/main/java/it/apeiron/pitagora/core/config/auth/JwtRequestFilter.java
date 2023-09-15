package it.apeiron.pitagora.core.config.auth;

import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.service.auth.JwtService;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		PitagoraUser userDetails = jwtService.validateToken(request);
		if (userDetails != null) {
			JwtAuthenticationToken token = new JwtAuthenticationToken(userDetails,
					Arrays.stream(userDetails.getRole().getAuthorities()).map(
							SimpleGrantedAuthority::new).collect(Collectors.toList()));
			SecurityContextHolder.getContext().setAuthentication(token);
		}
		chain.doFilter(request, response);
	}

}
