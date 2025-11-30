package com.tradingpt.tpt_api.global.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 부하테스트 전용 Security 설정
 *
 * loadtest.enabled=true 일 때만 활성화됩니다.
 * 운영 환경에서는 절대 활성화하지 마세요!
 *
 * 사용 방법:
 * 1. SPRING_PROFILES_ACTIVE=loadtest 로 서버 실행
 * 2. 요청 시 헤더 추가:
 *    - X-Load-Test-Auth: {secret-key}
 *    - X-Load-Test-User-Id: {가상 사용자 ID}
 *
 * 예시:
 * curl -H "X-Load-Test-Auth: tpt-loadtest-secret-2024" \
 *      -H "X-Load-Test-User-Id: 12345" \
 *      https://api.example.com/api/v1/memo
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "loadtest.enabled", havingValue = "true")
public class LoadTestSecurityConfig {

	@Value("${loadtest.secret-key:tpt-loadtest-secret-2024}")
	private String loadTestSecretKey;

	@Value("${loadtest.auth-bypass-header:X-Load-Test-Auth}")
	private String authBypassHeader;

	/**
	 * 부하테스트용 SecurityFilterChain
	 * 가장 높은 우선순위로 설정하여 다른 Security 체인보다 먼저 적용
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain loadTestSecurityFilterChain(HttpSecurity http) throws Exception {
		log.warn("⚠️ LoadTest Security Configuration is ENABLED. DO NOT use in production!");

		http
			.securityMatcher("/**")
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.addFilterBefore(loadTestAuthFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * 부하테스트용 인증 우회 필터
	 * 특정 헤더와 시크릿 키가 일치하면 가상 사용자로 인증 처리
	 */
	@Bean
	public OncePerRequestFilter loadTestAuthFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(
				HttpServletRequest request,
				HttpServletResponse response,
				FilterChain filterChain
			) throws ServletException, IOException {

				String authHeader = request.getHeader(authBypassHeader);

				// 부하테스트 헤더가 있고 시크릿 키가 일치하는 경우
				if (authHeader != null && authHeader.equals(loadTestSecretKey)) {
					// 가상 사용자 ID 추출 (없으면 기본값 1)
					String userIdHeader = request.getHeader("X-Load-Test-User-Id");
					Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : 1L;

					// 가상 사용자 역할 (기본: CUSTOMER)
					String roleHeader = request.getHeader("X-Load-Test-Role");
					String role = roleHeader != null ? roleHeader : "ROLE_CUSTOMER";

					// 가상 사용자명
					String usernameHeader = request.getHeader("X-Load-Test-Username");
					String username = usernameHeader != null ? usernameHeader : "loadtest_user_" + userId;

					// AuthSessionUser 생성 (실제 인증과 동일한 Principal 구조)
					AuthSessionUser testUser = new AuthSessionUser(userId, username, role);

					// Spring Security Authentication 설정
					UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(
							testUser,
							null,
							List.of(new SimpleGrantedAuthority(role))
						);

					SecurityContextHolder.getContext().setAuthentication(authentication);

					log.debug("LoadTest auth bypass: userId={}, username={}, role={}",
						userId, username, role);
				}

				filterChain.doFilter(request, response);
			}
		};
	}
}
