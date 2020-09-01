package com.kiran.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${ldap.url}")
	private String ldapUrl;

	@Value("${ldap.domain1}")
	private String ldapDomain1;

	@Value("${ldap.domain2}")
	private String ldapDomain2;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// to disable security of post, put and delete operations
		http.cors();

		// To disable spring security basic authentication.By default it is enable in
		// spring security.
		// http.httpBasic().disable();

		// Spring security form login. A default login form will be provided by spring
		// boot and take care of logout. Doesn't work with angular redirect.
		// http.authorizeRequests().anyRequest().fullyAuthenticated().and().formLogin().and().logout().permitAll();

		// Spring security http basic login, works with angular. Logout will not work.
		http.authorizeRequests().anyRequest().fullyAuthenticated().and().httpBasic().and().logout().permitAll();

	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Embedded ldap(LDIF) authentication.
		if (activeProfile.contentEquals("dev")) {
			auth.ldapAuthentication().userDnPatterns("uid={0},ou=people").groupSearchBase("ou=groups").contextSource()
					.url("ldap://localhost:8389/dc=springframework,dc=org").and().passwordCompare()
					// .passwordEncoder(new BCryptPasswordEncoder())
					.passwordAttribute("userPassword");
		} else {
			// AD LDAP authentication
			auth.authenticationProvider(adProvider1());
			// auth.authenticationProvider(adProvider2());
		}
	}

	@Bean
	public ActiveDirectoryLdapAuthenticationProvider adProvider1() {
		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
		return new ActiveDirectoryLdapAuthenticationProvider(ldapDomain1, ldapUrl);
	}

	@Bean
	public ActiveDirectoryLdapAuthenticationProvider adProvider2() {
		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
		return new ActiveDirectoryLdapAuthenticationProvider(ldapDomain2, ldapUrl);
	}

	@Component
	public class MyApplicationListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
		@Override
		public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
			Object userName = event.getAuthentication().getPrincipal();
			System.out.println("Login attempt failed by User: " + userName);
			System.out.println("Login Error: " + event.getException().getCause().getCause().getMessage());
		}
	}
}