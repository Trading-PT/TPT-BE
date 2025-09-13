package com.tradingpt.tpt_api.global.config;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.domain.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.domain.auth.handler.CustomSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;
	private final RememberMeServices rememberMeServices;

	@Bean
	public static HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	/** PasswordEncoder만 빈으로 제공하면 Spring이 내부적으로 DaoAuthenticationProvider를 구성합니다. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** AuthenticationManager는 AuthenticationConfiguration에서 주입받아 노출 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** 동시 세션 제어에 필요한 빈들 */
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	/** JSON 로그인 필터: 외부에서 AuthenticationManager 주입 */
	@Bean
	public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		JsonUsernamePasswordAuthFilter filter = new JsonUsernamePasswordAuthFilter(objectMapper);
		filter.setFilterProcessesUrl("/api/v1/auth/login");
		filter.setAuthenticationManager(authManager);          // ★ 자동 구성된 DaoAuthenticationProvider를 사용
		filter.setAuthenticationSuccessHandler(customSuccessHandler);
		filter.setAuthenticationFailureHandler(customFailureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			SessionRegistry sessionRegistry,
			JsonUsernamePasswordAuthFilter jsonLoginFilter
	) throws Exception {

		http
				// DaoAuthenticationProvider 빈 직접 주입 불필요(.authenticationProvider(...) 제거)
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
						.requestMatchers("/actuator/health").permitAll()  // 이 설정이 없으면 차단됨
						.requestMatchers("/actuator/**").permitAll()      // 모든 actuator 엔드포인트 허용
						.anyRequest().authenticated()
				)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.rememberMe(rm -> rm.rememberMeServices(rememberMeServices))
				.addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
