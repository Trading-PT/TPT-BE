package com.tradingpt.tpt_api.global.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.exception.code.GlobalErrorStatus;

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

		log.warn("[JsonAccessDeniedHandler] Access denied for URI: {}", request.getRequestURI());

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		BaseResponse<Void> body = BaseResponse.of(GlobalErrorStatus._FORBIDDEN, null);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
