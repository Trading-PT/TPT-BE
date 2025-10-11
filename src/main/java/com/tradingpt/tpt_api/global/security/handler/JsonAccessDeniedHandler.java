package com.tradingpt.tpt_api.global.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

/**
 * 인증은 되었지만 권한이 부족한 사용자가 리소스에 접근하려 할 때 처리
 * HTTP 403 (Forbidden) 응답 반환
 */
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

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = (auth != null && auth.isAuthenticated())
			? auth.getName()
			: "anonymous";

		String uri = request.getRequestURI();
		String method = request.getMethod();

		log.warn("[JsonAccessDeniedHandler] Access denied - User: {}, Method: {}, URI: {}",
			username, method, uri);

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		// URI에 따라 적절한 에러 코드 선택
		BaseCodeInterface errorCode = selectErrorCode(uri);

		BaseResponse<Void> body = BaseResponse.onFailure(errorCode, null);

		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

	private BaseCodeInterface selectErrorCode(String uri) {
		// Admin 경로인 경우
		if (uri.contains("/admin")) {
			return AuthErrorStatus.ACCESS_DENIED_ADMIN;
		}

		// 일반적인 권한 부족
		return AuthErrorStatus.ACCESS_DENIED_GENERAL;
	}
}
