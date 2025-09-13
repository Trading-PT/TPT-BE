package com.tradingpt.tpt_api.domain.auth.handler;

import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2User;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	private final RememberMeServices rememberMeServices;
	private final UserRepository userRepository;

	@Value("${app.frontend.base-url:http://localhost:3000}")
	private String frontendBaseUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {

		Object principal = authentication.getPrincipal();

		// 로컬(JSON) 로그인은 200으로 종료
		if (principal instanceof CustomUserDetails) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		// 소셜 로그인: remember-me 발급 후 휴대폰 번호만으로 분기
		if (principal instanceof CustomOAuth2User oAuth2User) {
			rememberMeServices.loginSuccess(request, response, authentication);

			Long userId = oAuth2User.getUserId();
			// userId 없거나 조회 실패면 번호 확인 불가 → 추가정보 필요로 간주
			if (userId == null) {
				response.sendRedirect(frontendBaseUrl + "/signup");
				return;
			}

			Optional<User> userOpt = userRepository.findById(userId);
			boolean needExtra = true;

			if (userOpt.isPresent() && userOpt.get() instanceof Customer c) {
				String phone = c.getPhoneNumber();
				needExtra = (phone == null || phone.trim().isEmpty());
			}

			response.sendRedirect(frontendBaseUrl + (needExtra ? "/signup" : "/login"));
			return;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "알 수 없는 사용자 타입");
	}
}
