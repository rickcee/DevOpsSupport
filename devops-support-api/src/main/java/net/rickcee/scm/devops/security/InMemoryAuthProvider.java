/**
 * 
 */
package net.rickcee.scm.devops.security;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rickcee
 *
 */
//@Component
@Slf4j
@Getter
public class InMemoryAuthProvider implements AuthenticationProvider {

	@Autowired
	private InMemoryUserDetailsManager userDetails;
	// For simplicity...
	private Map<String, String> userFullNames = new HashMap<>();
	
	@PostConstruct
	public void init() {
		userDetails.createUser(User.withUsername("catalrc").password("123").authorities("ROLE_REFDATA").build());
		userDetails.createUser(User.withUsername("filhor").password("456").authorities("ROLE_TRADE_ENTRY").build());

		userFullNames.put("catalrc", "Ricardo Catalfo");
		userFullNames.put("filhor", "Rogerio Filho");
	}
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		Object password = authentication.getCredentials();

		UserDetails ud = userDetails.loadUserByUsername(username);
		if (ud == null) {
			String msg = "User does not exist in MEM domain.";
			log.info(msg);
			throw new UsernameNotFoundException(msg);
		}

		if (ud.getPassword().equals(password)) {
//			DomainUsernamePasswordAuthenticationToken token = new DomainUsernamePasswordAuthenticationToken(username, password, ud.getAuthorities());
//			Map<String, String> details = new HashMap<>();
//			details.put("fullName", userFullNames.get(username));
//			token.setDetails(details);
//			return token;
			return authentication;
		}
		log.info("Username [" + username + "] provided invalid login credentials.");
		throw new BadCredentialsException("Invalid Credentials supplied!");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
