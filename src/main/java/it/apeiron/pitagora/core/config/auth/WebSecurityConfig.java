package it.apeiron.pitagora.core.config.auth;

import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.ADMIN;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.ANALYST;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.SUPER_ADMIN;
import static it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role.USER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {

		httpSecurity.csrf().disable()
				.cors().and()
				// dont authenticate this particular request
				.authorizeRequests()
				.antMatchers("/status").permitAll()
				.antMatchers("/auth/login").permitAll()
				.antMatchers("/auth/logout").permitAll()
				.antMatchers("/public").permitAll()
				.antMatchers("/thm/vedo/stream/**").permitAll()
				.antMatchers("/auth/changePassword").authenticated()
				.antMatchers("/auth/**").authenticated()
				.antMatchers("/source").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name()})
				.antMatchers("/model").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name()})
				.antMatchers("/mapper").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name()})
				.antMatchers("/chart").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name()})
				.antMatchers("/statistics").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name()})
				.antMatchers("/dataset").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name(), USER.name()})
				.antMatchers("/data").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name(), ANALYST.name(), USER.name()})
				.antMatchers("/alarm").hasAnyAuthority(new String[]{SUPER_ADMIN.name(), ADMIN.name()})
				// all other requests need to be authenticated
				.anyRequest().authenticated().and()
				// make sure we use stateless session; session won't be used to
				// store user's state.
				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, BasicAuthenticationFilter.class);
	}

//	@Bean
//	CorsConfigurationSource corsConfigurationSource() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//		return source;
//	}
}
