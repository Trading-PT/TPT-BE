package com.tradingpt.tpt_api.auth.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException {
		// 세션 쿠키(JSESSIONID)는 Spring Security가 응답 헤더(Set-Cookie)로 자동 발급
		CustomUserDetails principal = (CustomUserDetails)authentication.getPrincipal();

		BaseResponse<Map<String, Object>> body = BaseResponse.onSuccess(Map.of(
			"userId", principal.getId(),
			"username", principal.getUsername(),
			"role", principal.getRole().name()
		));

		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
