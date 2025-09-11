package com.tradingpt.tpt_api.domain.auth.controller;

import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.auth.service.AuthService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

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

	@Operation(summary = "내 정보 조회", description = "리멤버미로 인증된 현재 사용자 정보를 반환합니다. 리멤버미쿠키만 있으면 세션을 재발급 받고, 로그인이 가능합니다.")
	@GetMapping("/me")
	public BaseResponse<MeResponse> me(Authentication authentication) {
		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		MeResponse response = new MeResponse(principal.getId(), principal.getUsername(), principal.getRole().name());
		return BaseResponse.onSuccess(response);
	}
}
