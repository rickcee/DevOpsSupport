/**
 * 
 */
package net.rickcee.scm.devops.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rickcee
 *
 */
@Configuration
@EnableWebSecurity(debug = false)
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.csrf().disable().authorizeRequests()
				.antMatchers("/HealthCheck", "/actuator/**", "/jenkins**").permitAll()
				.antMatchers("/secured/**").authenticated()
				//.anyRequest().authenticated()
		
		.and().httpBasic()
		.and().logout().permitAll().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		.addLogoutHandler(new LogoutHandler() {
			
			@Override
			public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
				log.info("Invalidating HTTP Session...");
				request.getSession(false).invalidate();
			}
		})
		.logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("JSESSIONID")
			
		;
		
		// For H2 Console to Work...
		http.headers().frameOptions().sameOrigin();

		// @formatter:on
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**");
		web.ignoring().antMatchers("/js/**");
		web.ignoring().antMatchers("/images/**");
		web.ignoring().antMatchers("/fonts/**");
		web.ignoring().antMatchers("/json/**");
		web.ignoring().antMatchers("/angularservices/**");
		web.ignoring().antMatchers("/controllers/**");
		web.ignoring().antMatchers("/templates/**");
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		// return new BCryptPasswordEncoder();
		return NoOpPasswordEncoder.getInstance();
	}
	
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication()
//			.withUser("user").password("user").roles("USER").and()
//			.withUser("admin").password("admin").roles("ADMIN")
//		;
//	}
	
//	@Bean
//	public InMemoryUserDetailsManager inMemoryManager() {
//		log.info("Initializing In-Memory User Details Manager...");
//		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//		return manager;
//	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		  auth.inMemoryAuthentication().withUser("approver").password("approver").roles("ADMIN");
		  auth.inMemoryAuthentication().withUser("user").password("user").roles("RO");
		  auth.inMemoryAuthentication().withUser("auditor").password("auditor").roles("AUDIT");
	}
	
}
