package com.tradingpt.tpt_api.domain.auth.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2User;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	private final RememberMeServices rememberMeServices;
	private final UserRepository userRepository;
	private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	@Value("${app.frontend.base-url}")
	private String frontendBaseUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {

		Object principal = authentication.getPrincipal();

		// 1) 로컬(JSON) 로그인
		if (principal instanceof AuthSessionUser) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		// 2) 소셜 로그인
		if (principal instanceof CustomOAuth2User oAuth2User) {

			// 서비스에서 이미 매핑/생성을 끝냈다는 전제: userId가 반드시 존재해야 정상
			if (oAuth2User.getUserId() == null) {
				// 비정상 흐름 방어
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SOCIAL_NOT_MAPPED");
				return;
			}

			// 정상 로그인 처리
			AuthSessionUser sessionUser = new AuthSessionUser(
					oAuth2User.getUserId(),
					oAuth2User.getUsername(),
					oAuth2User.getRole()
			);

			Authentication safeAuth = new UsernamePasswordAuthenticationToken(
					sessionUser,
					null,
					oAuth2User.getAuthorities()
			);

			// remember-me는 최종 인증 객체(safeAuth)로 호출
			rememberMeServices.loginSuccess(request, response, safeAuth);

			// SecurityContext 저장
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(safeAuth);
			securityContextRepository.saveContext(context, request, response);

			// 추가 정보 필요 여부(전화번호 등) 체크 후 리다이렉트
			boolean needExtra = true;
			Optional<User> userOpt = userRepository.findById(oAuth2User.getUserId());
			if (userOpt.isPresent() && userOpt.get() instanceof Customer c) {
				String phone = c.getPhoneNumber();
				needExtra = (phone == null || phone.trim().isEmpty());
			}

			response.sendRedirect(frontendBaseUrl + (needExtra ? "/signup?social=true" : "/"));
			return;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNKNOWN_PRINCIPAL_TYPE");
	}
}
