package com.tradingpt.tpt_api.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2UserService;
import com.tradingpt.tpt_api.global.security.csrf.HeaderAndCookieCsrfTokenRepository;
import com.tradingpt.tpt_api.global.security.handler.JsonAccessDeniedHandler;
import com.tradingpt.tpt_api.global.security.handler.JsonAuthenticationEntryPoint;
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
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;
	private final RememberMeServices rememberMeServices;
	private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
	private final JsonAccessDeniedHandler jsonAccessDeniedHandler;
	private final ServerProperties serverProperties;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** 동시 세션 제어 */
	@Bean
	public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
		return new SpringSessionBackedSessionRegistry<>(sessionRepository);
	}
	@Bean
	public static HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	/** OAuth2 AuthorizationRequest 를 세션 대신 쿠키에 저장 */
	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper);
	}

	/** JSON 로그인 필터 */
	@Bean
	public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		var filter = new JsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/auth/login");
		filter.setAuthenticationManager(authManager);
		filter.setAuthenticationSuccessHandler(customSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			SessionRegistry sessionRegistry,
			JsonUsernamePasswordAuthFilter jsonLoginFilter,
			CustomOAuth2UserService customOAuth2UserService,
			AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthRequestRepository
	) throws Exception {

		var requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName("_csrf");

		HeaderAndCookieCsrfTokenRepository csrfTokenRepository = new HeaderAndCookieCsrfTokenRepository();
		var cookieProps = serverProperties.getServlet().getSession().getCookie();
		if (cookieProps.getDomain() != null) {
			csrfTokenRepository.setCookieDomain(cookieProps.getDomain());
		}
		if (cookieProps.getPath() != null) {
			csrfTokenRepository.setCookiePath(cookieProps.getPath());
		}
		// CSRF 쿠키는 JS에서 읽어야 하므로 httpOnly=false 유지
		csrfTokenRepository.setCookieHttpOnly(false);
		csrfTokenRepository.setCookieCustomizer(builder -> {
			if (cookieProps.getSecure() != null) {
				builder.secure(cookieProps.getSecure());
			}
			if (cookieProps.getSameSite() != null) {
				String sameSite;
				switch (cookieProps.getSameSite()) {
					case LAX -> sameSite = "Lax";
					case STRICT -> sameSite = "Strict";
					case NONE -> sameSite = "None";
					default -> sameSite = cookieProps.getSameSite().name();
				}
				builder.sameSite(sameSite);
			}
		});

		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/oauth2/**")
						.csrfTokenRepository(csrfTokenRepository)
						.csrfTokenRequestHandler(requestHandler)
				)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jsonAuthenticationEntryPoint)
						.accessDeniedHandler(jsonAccessDeniedHandler)
				)
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
								"/api/v1/auth/**",
								"/oauth2/**", "/login/oauth2/**",
								"/swagger-ui.html", "/swagger-ui/**",
								"/swagger-resources/**", "/webjars/**",
								"/v3/api-docs/**",
								"/actuator/**"
						).permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.oauth2Login(o -> o
						.authorizationEndpoint(ae -> ae
								.authorizationRequestRepository(cookieAuthRequestRepository) // ✅ 세션 대신 쿠키 저장소 사용
						)
						.userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
						.successHandler(customSuccessHandler)
						.failureHandler(customFailureHandler)
				)
				.rememberMe(rm -> rm
						.rememberMeServices(rememberMeServices)
						.rememberMeParameter("rememberMe")
						.alwaysRemember(false)
						.useSecureCookie(true)
				)
				.addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
