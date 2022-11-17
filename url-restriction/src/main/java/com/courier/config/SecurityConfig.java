package com.courier.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	DataSource dataSource;

	@Autowired
	PersistentTokenRepository tokenRepo;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth)
			throws Exception {

		auth.jdbcAuthentication()
				.dataSource(dataSource)

				.usersByUsernameQuery(
						"select username,password,enabled from users where username=?")
				.authoritiesByUsernameQuery(
						"select username, role from user_roles where username=?");

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.sessionManagement().maximumSessions(1);

		http.authorizeRequests().antMatchers("/images/**").permitAll()
				.antMatchers("/login*").permitAll()
				.antMatchers("/report*").hasRole("EMPLOYEE").anyRequest().authenticated()
				.antMatchers("/**").hasAnyRole("EMPLOYEE", "USER").anyRequest()
				.fullyAuthenticated().and().formLogin().loginPage("/login")
				.usernameParameter("username").passwordParameter("password")
				.loginProcessingUrl("/doLogin")
				.defaultSuccessUrl("/index", true).failureUrl("/accessDenied")
				.permitAll().and().exceptionHandling()
				.accessDeniedPage("/WEB-INF/pages/accessDenied.jsp").and()
				.logout().logoutUrl("/logout").logoutSuccessUrl("/login")
				.permitAll().deleteCookies("JSESSIONID")

				.and().rememberMe().rememberMeParameter("remember-me")
				.tokenRepository(tokenRepo).tokenValiditySeconds(86400);

	}

	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**").addResourceLocations(
				"/classpath:/static/images/**");
	}

}
