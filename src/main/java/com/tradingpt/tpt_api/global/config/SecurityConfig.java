package com.tradingpt.tpt_api.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;
	private final RememberMeServices rememberMeServices;

	/** AuthenticationManager는 AuthenticationConfiguration에서 주입받아 노출 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** 동시 세션 제어에 필요한 빈들 */
	@Bean public SessionRegistry sessionRegistry() { return new SessionRegistryImpl(); }
	@Bean public static HttpSessionEventPublisher httpSessionEventPublisher() { return new HttpSessionEventPublisher(); }

	/** JSON 로그인 필터 */
	@Bean
	public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		var filter = new JsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/auth/login");
		filter.setAuthenticationManager(authManager);
		filter.setAuthenticationSuccessHandler(customSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices); // 로컬(JSON) 로그인 remember-me 처리
		return filter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			SessionRegistry sessionRegistry,
			JsonUsernamePasswordAuthFilter jsonLoginFilter,
			CustomOAuth2UserService customOAuth2UserService   // 메서드 파라미터로 주입(순환참조 방지)
	) throws Exception {

		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/auth/**", "/oauth2/**"))
				.sessionManagement(sm -> sm
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation(sf -> sf.migrateSession())
						.sessionConcurrency(sc -> sc
								.maximumSessions(3)
								.maxSessionsPreventsLogin(false)
								.sessionRegistry(sessionRegistry)
						)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/auth/**", "/oauth2/**",
								"/swagger-ui.html", "/swagger-ui/**",
								"/swagger-resources/**", "/webjars/**",
								"/v3/api-docs/**"
						).permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/actuator/**").permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.oauth2Login(o -> o
						.userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
						.successHandler(customSuccessHandler)   // 소셜 성공 시: 핸들러에서 remember-me + 리다이렉트
						.failureHandler(customFailureHandler)
				)
				.rememberMe(rm -> rm
						.rememberMeServices(rememberMeServices)
						.rememberMeParameter("remember-me")
						.alwaysRemember(false)
						.useSecureCookie(true) // 배포 HTTPS에서 Secure 쿠키
				)
				.addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
