package com.tradingpt.tpt_api.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomFailureHandler implements AuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
										HttpServletResponse response,
										AuthenticationException exception) throws IOException {

		BaseCodeInterface errorCode = AuthErrorStatus.AUTHENTICATION_FAILED;

		// 폼/JSON 로그인 쪽 예외 매핑
		if (exception instanceof BadCredentialsException) {
			errorCode = AuthErrorStatus.BAD_CREDENTIALS;  // "아이디 또는 비밀번호가 올바르지 않습니다."
		} else if (exception instanceof DisabledException) {
			errorCode = AuthErrorStatus.ACCOUNT_DISABLED;  // "비활성화된 계정입니다."
		} else if (exception instanceof LockedException) {
			errorCode = AuthErrorStatus.ACCOUNT_LOCKED;  // "잠긴 계정입니다."
		} else if (exception instanceof CredentialsExpiredException) {
			errorCode = AuthErrorStatus.CREDENTIALS_EXPIRED;  // "비밀번호 유효 기간이 만료되었습니다."
		} else if (exception instanceof AccountExpiredException) {
			errorCode = AuthErrorStatus.ACCOUNT_EXPIRED;  // "계정 유효 기간이 만료되었습니다."
		}
		// 소셜 로그인(OAuth2) 실패
		else if (exception instanceof OAuth2AuthenticationException) {
			errorCode = AuthErrorStatus.OAUTH2_AUTHENTICATION_FAILED;  // "소셜 로그인 인증에 실패했습니다. 다시 시도해 주세요."
		}

		BaseResponse<Void> body = BaseResponse.onFailure(errorCode, null);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
