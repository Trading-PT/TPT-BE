package com.tradingpt.tpt_api.global.config;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.filter.AdminJsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.filter.CsrfTokenResponseHeaderBindingFilter;
import com.tradingpt.tpt_api.domain.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.handler.AdminSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomSuccessHandler;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2UserService;
import com.tradingpt.tpt_api.global.security.csrf.HeaderAndCookieCsrfTokenRepository;
import com.tradingpt.tpt_api.global.security.handler.JsonAccessDeniedHandler;
import com.tradingpt.tpt_api.global.security.handler.JsonAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

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
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Bean
	public static HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	@Primary
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** 세션 동시 접속 제어 */
	@Bean
	public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
		return new SpringSessionBackedSessionRegistry<>(sessionRepository);
	}

	/**
	 * OAuth2 AuthorizationRequest를 세션 대신 쿠키에 저장
	 * HttpCookieOAuth2AuthorizationRequestRepository는 @Component로 등록되어 있으므로
	 * 별도 @Bean 정의 없이 자동 주입됨
	 */

	/** 사용자 전용 Provider: ADMIN/TRAINER 계정은 사용자 로그인에서 인증 실패 */
	@Bean(name = "userAuthProvider")
	public AuthenticationProvider userAuthProvider() {
		DaoAuthenticationProvider p = new DaoAuthenticationProvider();
		p.setUserDetailsService(username -> {
			UserDetails u = userDetailsService.loadUserByUsername(username);
			for (GrantedAuthority a : u.getAuthorities()) {
				String g = a.getAuthority();
				if ("ROLE_ADMIN".equals(g) || "ROLE_TRAINER".equals(g)) {
					throw new UsernameNotFoundException("User-only login endpoint");
				}
			}
			return u;
		});
		p.setPasswordEncoder(passwordEncoder);
		return p;
	}

	/** 사용자 전용 AuthenticationManager */
	@Bean(name = "userAuthenticationManager")
	public AuthenticationManager userAuthenticationManager(
		@Qualifier("userAuthProvider") AuthenticationProvider userProvider) {
		return new ProviderManager(java.util.List.of(userProvider));
	}

	/** 관리자 전용 Provider: 비관리자는 관리자 로그인에서 인증 실패 */
	@Bean(name = "adminAuthProvider")
	public AuthenticationProvider adminAuthProvider() {
		DaoAuthenticationProvider p = new DaoAuthenticationProvider();
		p.setUserDetailsService(username -> {
			UserDetails u = userDetailsService.loadUserByUsername(username);
			if (!hasAdminOrTrainer(u.getAuthorities())) {
				throw new UsernameNotFoundException("Admin-only login endpoint");
			}
			return u;
		});
		p.setPasswordEncoder(passwordEncoder);
		return p;
	}

	private boolean hasAdminOrTrainer(Collection<? extends GrantedAuthority> auths) {
		for (GrantedAuthority a : auths) {
			String g = a.getAuthority();
			if ("ROLE_ADMIN".equals(g) || "ROLE_TRAINER".equals(g))
				return true;
		}
		return false;
	}

	/** 관리자 전용 AuthenticationManager */
	@Bean(name = "adminAuthenticationManager")
	public AuthenticationManager adminAuthenticationManager(
		@Qualifier("adminAuthProvider") AuthenticationProvider adminProvider) {
		return new ProviderManager(java.util.List.of(adminProvider));
	}

	/** JSON 로그인 필터 (사용자용) — 사용자 전용 AuthenticationManager 주입 */
	@Bean
	public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(
		@Qualifier("userAuthenticationManager") AuthenticationManager userAuthManager) {
		var filter = new JsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/auth/login"); // 사용자 로그인 엔드포인트
		filter.setAuthenticationManager(userAuthManager);    // ★ 사용자 전용 매니저 사용
		filter.setAuthenticationSuccessHandler(customSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	/** 관리자 로그인 필터 — 관리자 전용 AuthenticationManager 주입 */
	@Bean
	public AdminJsonUsernamePasswordAuthFilter adminJsonUsernamePasswordAuthFilter(
		@Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthManager) {
		var filter = new AdminJsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/admin/login");
		filter.setAuthenticationManager(adminAuthManager);   // ★ 관리자 전용 매니저 사용
		filter.setAuthenticationSuccessHandler(adminSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	/** 공용 CSRF TokenRepository (쿠키+헤더 동시 사용) */
	@Bean
	public HeaderAndCookieCsrfTokenRepository csrfTokenRepository() {
		HeaderAndCookieCsrfTokenRepository repo = new HeaderAndCookieCsrfTokenRepository();
		var cookieProps = serverProperties.getServlet().getSession().getCookie();
		if (cookieProps.getDomain() != null)
			repo.setCookieDomain(cookieProps.getDomain());
		if (cookieProps.getPath() != null)
			repo.setCookiePath(cookieProps.getPath());
		repo.setCookieHttpOnly(false);
		repo.setCookieCustomizer(builder -> {
			if (cookieProps.getSecure() != null)
				builder.secure(cookieProps.getSecure());
			if (cookieProps.getSameSite() != null) {
				String sameSite = switch (cookieProps.getSameSite()) {
					case LAX -> "Lax";
					case STRICT -> "Strict";
					case NONE -> "None";
					default -> "Lax";
				};
				builder.sameSite(sameSite);
			} else
				builder.sameSite("Lax");
		});
		return repo;
	}

	/* =========================
	 * Security Filter Chains
	 * ========================= */

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

		var adminMatcher = new RegexRequestMatcher("^/api/v1/admin(?:/.*)?$", null);

		http.securityMatcher(adminMatcher)
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf
				.ignoringRequestMatchers("/api/v1/admin/login")
				.csrfTokenRepository(csrfTokenRepository)
				.csrfTokenRequestHandler(requestHandler)
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(jsonAuthenticationEntryPoint)
				.accessDeniedHandler(jsonAccessDeniedHandler)
			)
			.sessionManagement(sm -> sm
				.sessionCreationPolicy(SessionCreationPolicy.NEVER) // 절대 자동으로 세션 만들지 말라
				.sessionFixation(sf -> sf.migrateSession())
				.sessionConcurrency(sc -> sc.maximumSessions(5).sessionRegistry(sessionRegistry))
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/v1/admin/login").permitAll()
				.anyRequest().hasAnyRole("ADMIN", "TRAINER")
			)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.rememberMe(rm -> rm
				.rememberMeServices(rememberMeServices)
				.useSecureCookie(true)
			)
			.addFilterAt(adminJsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new CsrfTokenResponseHeaderBindingFilter(csrfTokenRepository),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	@Order(1)
	public SecurityFilterChain userSecurityFilterChain(
		HttpSecurity http,
		SessionRegistry sessionRegistry,
		JsonUsernamePasswordAuthFilter jsonLoginFilter,
		CustomOAuth2UserService customOAuth2UserService,
		HeaderAndCookieCsrfTokenRepository csrfTokenRepository
	) throws Exception {

		var requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName("_csrf");

		// OAuth2 경로 포함을 위해 OrRequestMatcher 사용
		var userApiMatcher = new OrRequestMatcher(
			new RegexRequestMatcher("^/api/(?!v1/admin(?:/|$)).*$", null),
			new AntPathRequestMatcher("/oauth2/**"),
			new AntPathRequestMatcher("/login/oauth2/**")
		);

		http.securityMatcher(userApiMatcher)
			.cors(Customizer.withDefaults())
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
				.sessionConcurrency(sc -> sc.maximumSessions(3).sessionRegistry(sessionRegistry))
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/auth/**",
					"/oauth2/**", "/login/oauth2/**"
				).permitAll()
				// ✅ 피드백 목록 및 상세 모두 비로그인 허용
				.requestMatchers(HttpMethod.GET, "/api/v1/feedback-requests").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/feedback-requests/*").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/columns").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/reviews").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/reviews/statistics").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.oauth2Login(o -> o
				.userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
				.successHandler(customSuccessHandler)
				.failureHandler(customFailureHandler)
			)
			.rememberMe(rm -> rm.rememberMeServices(rememberMeServices).useSecureCookie(true))
			.addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new CsrfTokenResponseHeaderBindingFilter(csrfTokenRepository),
				UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
}
