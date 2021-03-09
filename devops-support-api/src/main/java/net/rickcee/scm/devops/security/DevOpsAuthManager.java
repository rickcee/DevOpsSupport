/**
 * 
 */
package net.rickcee.scm.devops.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rickcee
 *
 */
@Slf4j
//@Component
public class DevOpsAuthManager implements AuthenticationManager {
	@Autowired
	private InMemoryAuthProvider memoryAuthManager;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		return memoryAuthManager.authenticate(authentication);
	}

}
