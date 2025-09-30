package com.tradingpt.tpt_api.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.filter.AdminJsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.filter.CsrfTokenResponseHeaderBindingFilter;
import com.tradingpt.tpt_api.domain.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.handler.AdminSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2UserService;
import com.tradingpt.tpt_api.global.security.csrf.HeaderAndCookieCsrfTokenRepository;
import com.tradingpt.tpt_api.global.security.handler.JsonAccessDeniedHandler;
import com.tradingpt.tpt_api.global.security.handler.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;
	private final AdminSuccessHandler adminSuccessHandler;
	private final RememberMeServices rememberMeServices;
	private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
	private final JsonAccessDeniedHandler jsonAccessDeniedHandler;
	private final ServerProperties serverProperties;

	/** AuthenticationManager */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** 세션 동시 접속 제어 */
	@Bean
	public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
		return new SpringSessionBackedSessionRegistry<>(sessionRepository);
	}

	@Bean
	public static HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	/** OAuth2 AuthorizationRequest를 세션 대신 쿠키에 저장 */
	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper);
	}

	/** JSON 로그인 필터 (사용자용) */
	@Bean
	public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		var filter = new JsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/auth/login"); // ← 사용자 로그인 엔드포인트
		filter.setAuthenticationManager(authManager);
		filter.setAuthenticationSuccessHandler(customSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	/** 관리자 로그인 필터 */
	@Bean
	public AdminJsonUsernamePasswordAuthFilter adminJsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		var filter = new AdminJsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/admin/api/v1/login"); // ★ 반드시 슬래시 포함 + /admin prefix
		filter.setAuthenticationManager(authManager);
		filter.setAuthenticationSuccessHandler(adminSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	/** 공용 CSRF TokenRepository */
	@Bean
	public HeaderAndCookieCsrfTokenRepository csrfTokenRepository() {
		HeaderAndCookieCsrfTokenRepository repo = new HeaderAndCookieCsrfTokenRepository();
		var cookieProps = serverProperties.getServlet().getSession().getCookie();
		if (cookieProps.getDomain() != null) repo.setCookieDomain(cookieProps.getDomain());
		if (cookieProps.getPath() != null) repo.setCookiePath(cookieProps.getPath());
		repo.setCookieHttpOnly(false);
		repo.setCookieCustomizer(builder -> {
			if (cookieProps.getSecure() != null) builder.secure(cookieProps.getSecure());
			if (cookieProps.getSameSite() != null) {
				String sameSite = switch (cookieProps.getSameSite()) {
					case LAX -> "Lax";
					case STRICT -> "Strict";
					case NONE -> "None";
					default -> "Lax";
				};
				builder.sameSite(sameSite);
			} else builder.sameSite("Lax");
		});
		return repo;
	}

	/** -------------------- ADMIN -------------------- */
	@Bean
	@Order(0)
	public SecurityFilterChain adminSecurityFilterChain(
			HttpSecurity http,
			SessionRegistry sessionRegistry,
			AdminJsonUsernamePasswordAuthFilter adminJsonLoginFilter,
			HeaderAndCookieCsrfTokenRepository csrfTokenRepository
	) throws Exception {

		var requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName("_csrf");

		http.securityMatcher("/admin/**") // ★ /admin으로 시작하면 전부 이 체인
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/admin/api/v1/login") // ★ 같은 경로
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
						.sessionConcurrency(sc -> sc.maximumSessions(1)
								.sessionRegistry(sessionRegistry))
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/admin/api/v1/login").permitAll() // ★ 같은 경로
						.anyRequest().hasAnyRole("ADMIN", "TRAINER")
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.rememberMe(rm -> rm
						.rememberMeServices(rememberMeServices)
						.useSecureCookie(true)
				)
				.addFilterAt(adminJsonLoginFilter, UsernamePasswordAuthenticationFilter.class);



		return http.build();
	}

	/** -------------------- USER -------------------- */
	@Bean
	@Order(1)
	public SecurityFilterChain userSecurityFilterChain(
			HttpSecurity http,
			SessionRegistry sessionRegistry,
			JsonUsernamePasswordAuthFilter jsonLoginFilter,
			CustomOAuth2UserService customOAuth2UserService,
			AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthRequestRepository,
			HeaderAndCookieCsrfTokenRepository csrfTokenRepository
	) throws Exception {

		var requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName("_csrf");

		http.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/**")
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
						.sessionConcurrency(sc -> sc.maximumSessions(3)
								.sessionRegistry(sessionRegistry))
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/auth/**",
								"/oauth2/**", "/login/oauth2/**",
								"/swagger-ui.html", "/swagger-ui/**",
								"/swagger-resources/**", "/webjars/**",
								"/v3/api-docs/**", "/actuator/**",
								"/", "/error", "/favicon.ico"
						).permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.oauth2Login(o -> o
						.authorizationEndpoint(ae -> ae
								.authorizationRequestRepository(cookieAuthRequestRepository)
						)
						.userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
						.successHandler(customSuccessHandler)
						.failureHandler(customFailureHandler)
				)
				.rememberMe(rm -> rm
						.rememberMeServices(rememberMeServices)
						.useSecureCookie(true)
				)
				.addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new CsrfTokenResponseHeaderBindingFilter(csrfTokenRepository),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
