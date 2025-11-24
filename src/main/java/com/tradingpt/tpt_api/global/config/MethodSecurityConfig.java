package com.tradingpt.tpt_api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method Security 설정
 *
 * @PreAuthorize, @PostAuthorize, @Secured 등의 메서드 레벨 보안 어노테이션을 활성화
 *
 * Spring Security 6.0+ (Spring Boot 3.0+)부터는 명시적으로 설정 필요
 *
 * 주요 기능:
 * - @PreAuthorize: 메서드 실행 전 권한 체크 (가장 많이 사용)
 * - @PostAuthorize: 메서드 실행 후 권한 체크
 * - @Secured: 간단한 역할 기반 체크
 *
 * 예시:
 * <pre>
 * {@code
 * @PreAuthorize("hasRole('ROLE_CUSTOMER')")
 * public void customerOnlyMethod() { ... }
 *
 * @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
 * public void adminOrTrainerMethod() { ... }
 * }
 * </pre>
 *
 * 권한 체크 실패 시:
 * - AccessDeniedException 발생
 * - ExceptionTranslationFilter가 캐치
 * - JsonAccessDeniedHandler가 처리 (403 응답)
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
	// Spring Security 6.0+의 기본 설정 사용
	// 추가 커스터마이징이 필요한 경우 이 클래스에서 Bean 정의
}
