package com.tradingpt.tpt_api.global.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
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
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {

		String uri = request.getRequestURI();
		String method = request.getMethod();

		log.warn("[JsonAuthenticationEntryPoint] Unauthorized access - Method: {}, URI: {}, Exception: {}",
			method, uri, authException.getClass().getSimpleName());

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
			return AuthErrorStatus.AUTHENTICATION_REQUIRED_ADMIN;
		}

		// 일반적인 인증 실패
		return AuthErrorStatus.AUTHENTICATION_REQUIRED;
	}
}
