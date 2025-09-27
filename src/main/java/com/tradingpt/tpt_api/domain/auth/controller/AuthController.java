package com.tradingpt.tpt_api.domain.auth.controller;

import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.SocialInfoResponse;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.auth.service.AuthService;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.user.service.UserService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.exception.AuthException;
import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.logout.LogoutHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 인가", description = "인증,인가 관련 API")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final UserService userService;
	private final LogoutHelper logoutHelper;

	@Operation(summary = "휴대폰 인증코드 발송", description = "휴대폰 번호로 6자리 인증코드를 발송하고 세션에 OTP/만료시각을 저장합니다.")
	@PostMapping("/phone/code")
	public BaseResponse<Void> sendPhoneCode(@Valid @RequestBody SendPhoneCodeRequest req,
											HttpSession session) {
		authService.sendPhoneCode(req, session);
		return BaseResponse.onSuccess(null);
	}

	@Operation(summary = "휴대폰 인증코드 검증", description = "세션에 저장된 OTP와 사용자가 입력한 코드를 검증합니다.")
	@PostMapping("/phone/verify")
	public BaseResponse<Void> verifyPhone(@Valid @RequestBody VerifyCodeRequest req,
										  HttpSession session) {
		authService.verifyPhoneCode(req, session);
		return BaseResponse.onSuccess(null);
	}

	@Operation(summary = "이메일 인증코드 발송", description = "이메일로 인증코드를 발송하고 세션에 OTP/만료시각을 저장합니다.")
	@PostMapping("/email/code")
	public BaseResponse<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest req,
											HttpSession session) {
		authService.sendEmailCode(req, session);
		return BaseResponse.onSuccess(null);
	}

	@Operation(summary = "이메일 인증코드 검증", description = "세션에 저장된 OTP와 사용자가 입력한 코드를 검증합니다.")
	@PostMapping("/email/verify")
	public BaseResponse<Void> verifyEmail(@Valid @RequestBody VerifyCodeRequest req,
										  HttpSession session) {
		authService.verifyEmailCode(req, session);
		return BaseResponse.onSuccess(null);
	}

	@Operation(summary = "회원가입", description = "휴대폰/이메일 인증을 통과한 사용자를 가입 처리합니다.")
	@PostMapping("/signup")
	public BaseResponse<Void> signUp(@Valid @RequestBody SignUpRequest req, HttpSession session) {
		authService.signUp(req, session);
		return BaseResponse.onSuccessCreate(null);
	}

	@Operation(summary = "아이디(사용자명) 중복 체크", description = "해당 사용자명이 사용 가능한지 여부를 반환합니다. true=사용 가능, false=이미 존재.")
	@GetMapping("/username/available")
	public BaseResponse<Boolean> isUsernameAvailable(@RequestParam String username) {
		return BaseResponse.onSuccess(authService.isUsernameAvailable(username));
	}

	@Operation(summary = "내 정보 조회", description = "세션 쿠키로 인증된 현재 사용자 정보를 반환합니다.")
	@GetMapping("/me")
	public BaseResponse<MeResponse> me(Authentication authentication) {
		AuthSessionUser principal = (AuthSessionUser) authentication.getPrincipal();
		MeResponse response = userService.getMe(principal.id());
		return BaseResponse.onSuccess(response);
	}

	@Operation(summary = "소셜 로그인 기본 정보 조회", description = "소셜 로그인 시 자동 채워질 사용자 기본 정보를 반환합니다.")
	@GetMapping("/social-info")
	public BaseResponse<SocialInfoResponse> getSocialInfo(Authentication authentication) {

		AuthSessionUser principal = (AuthSessionUser) authentication.getPrincipal();

		User user = userRepository.findById(principal.id())
				.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

		SocialInfoResponse dto = new SocialInfoResponse(
				user.getId(),
				user.getUsername(),
				user.getName(),
				user.getEmail(),
				user.getPassword() // 해시 그대로
		);

		return BaseResponse.onSuccess(dto);
	}
	@Operation(summary = "로그아웃", description = "로그아웃시 현재 로그인되어잇는 디바이스의 세션 쿠키 및 리멤버미 쿠키, 레디스 세션을 삭제합니다.")
	@PostMapping("/logout")
	public BaseResponse<Void> logout(HttpServletRequest req, HttpServletResponse res, Authentication auth) {
		logoutHelper.logoutCurrentRequest(req, res, auth, CookieProps.defaults());
		return BaseResponse.onSuccess(null);
	}

	@Operation(summary = "회원탈퇴", description = "해당 디바이스의 세션 쿠키 및 리멤버미 쿠키, 해당 계정 레디스 세션 전부를 삭제합니다.")
	@DeleteMapping("/users")
	public BaseResponse<Void> withdraw(Authentication auth,
									   HttpServletRequest req,
									   HttpServletResponse res) {
		var principal = (AuthSessionUser) auth.getPrincipal();
		userService.deleteAccount(principal.id());

		logoutHelper.invalidateAllDevices(principal.username());
		logoutHelper.logoutCurrentRequest(req, res, auth, CookieProps.defaults());
		return BaseResponse.onSuccess(null);
	}
}
