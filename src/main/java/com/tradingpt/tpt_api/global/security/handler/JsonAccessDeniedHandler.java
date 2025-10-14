package com.tradingpt.tpt_api.global.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {

		String uri = request.getRequestURI();
		String method = request.getMethod();
		String username = request.getUserPrincipal() != null ?
			request.getUserPrincipal().getName() : "anonymous";

		log.warn("[JsonAccessDeniedHandler] Access denied - User: {}, Method: {}, URI: {}, Exception: {}",
			username, method, uri, accessDeniedException.getMessage());

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		BaseCodeInterface errorCode = selectErrorCode(uri, accessDeniedException);

		BaseResponse<Void> body = BaseResponse.onFailure(errorCode, null);

		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

	/**
	 * URI와 예외 타입에 따라 적절한 에러 코드 선택
	 */
	private BaseCodeInterface selectErrorCode(String uri, AccessDeniedException exception) {
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// 1. CSRF 에러 체크 (최우선)
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		if (isCsrfException(exception)) {
			log.warn("[JsonAccessDeniedHandler] CSRF token validation failed");
			return AuthErrorStatus.CSRF_TOKEN_INVALID;
		}

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// 2. Admin 경로 권한 부족
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		if (uri.contains("/admin")) {
			return AuthErrorStatus.ACCESS_DENIED_ADMIN;
		}

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// 3. 일반 권한 부족
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		return AuthErrorStatus.ACCESS_DENIED_GENERAL;
	}

	/**
	 * CSRF 예외인지 판단
	 *
	 * CsrfFilter는 다음 메시지로 예외를 던짐:
	 * "Invalid CSRF token found for http://..."
	 */
	private boolean isCsrfException(AccessDeniedException exception) {
		String message = exception.getMessage();

		// 방법 1: 메시지 체크
		if (message != null && message.toLowerCase().contains("csrf")) {
			return true;
		}

		// 방법 2: 예외 클래스 체크 (Spring Security 6+)
		// MissingCsrfTokenException 또는 InvalidCsrfTokenException
		String exceptionClass = exception.getClass().getSimpleName();
		if (exceptionClass.contains("Csrf")) {
			return true;
		}

		return false;
	}
}