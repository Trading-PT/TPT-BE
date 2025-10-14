package com.tradingpt.tpt_api.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.user.dto.request.ChangePasswordRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.ProfileImageResponseDTO;
import com.tradingpt.tpt_api.domain.user.service.UserService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.logout.LogoutHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "유저", description = "유저 관리 관련 API")
public class UserController {

	private final UserService userService;
	private final LogoutHelper logoutHelper;

	@Operation(summary = "로그인 상황에서 비밀번호 변경 후 전 디바이스 로그아웃", description = "비밀번호를 변경하고, 전 디바이스의 세션, 리멤버미 쿠키를 무효화합니다.")
	@PostMapping("/password/change")
	public ResponseEntity<BaseResponse<Void>> changePassword(
		Authentication auth,
		@Valid @RequestBody ChangePasswordRequestDTO req,
		HttpServletRequest httpReq,
		HttpServletResponse httpRes
	) {
		AuthSessionUser principal = (AuthSessionUser)auth.getPrincipal();
		userService.changePassword(principal.id(), req);

		// 보안상 전 디바이스 만료
		logoutHelper.invalidateAllDevices(principal.username());
		// 현재 요청도 로그아웃 + 쿠키 만료
		logoutHelper.logoutCurrentRequest(httpReq, httpRes, auth, CookieProps.defaults());

		return ResponseEntity.ok(BaseResponse.onSuccess(null));
	}

	@Operation(summary = "프로필 이미지 수정", description = "프로필 이미지를 업로드하거나 교체합니다.")
	@PostMapping(value = "/profile-image", consumes = "multipart/form-data")
	public ResponseEntity<BaseResponse<ProfileImageResponseDTO>> updateProfileImage(
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@NotNull @RequestPart("file") MultipartFile file
	) {
		ProfileImageResponseDTO result = userService.updateProfileImage(customerId, file);
		return ResponseEntity.ok(BaseResponse.onSuccess(result));
	}
}
