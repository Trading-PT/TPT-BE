package com.tradingpt.tpt_api.domain.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.auth.dto.request.FindIdRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.response.FindIdResponse;
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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final UserService userService;

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
		AuthSessionUser principal = (AuthSessionUser)authentication.getPrincipal();
		MeResponse response = userService.getMe(principal.id());
		return BaseResponse.onSuccess(response);
	}

	@Operation(summary = "소셜 로그인 기본 정보 조회", description = "소셜 로그인 시 자동 채워질 사용자 기본 정보를 반환합니다.")
	@GetMapping("/social-info")
	public BaseResponse<SocialInfoResponse> getSocialInfo(Authentication authentication) {

		AuthSessionUser principal = (AuthSessionUser)authentication.getPrincipal();

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

	@Operation(summary = "이메일로 유저 ID 찾기", description = "email을 입력하면 해당 유저의 내부 PK(ID)를 반환합니다.")
	@PostMapping("/id/find")
	public BaseResponse<FindIdResponse> findIdByEmail(@Valid @RequestBody FindIdRequest req) {
		FindIdResponse response = userService.findUserId(req.getEmail());
		return BaseResponse.onSuccess(response);
	}

}
