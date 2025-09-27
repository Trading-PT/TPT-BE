package com.tradingpt.tpt_api.domain.user.controller;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.user.dto.request.ChangePasswordRequest;
import com.tradingpt.tpt_api.domain.user.dto.request.FindIdRequest;
import com.tradingpt.tpt_api.domain.user.dto.response.FindIdResponse;
import com.tradingpt.tpt_api.domain.user.service.UserService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.logout.LogoutHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "유저", description = "유저 관리 관련 API")
public class UserController {

    private final UserService userService;
    private final LogoutHelper logoutHelper;

    @Operation(summary = "이메일로 유저 ID 찾기", description = "email을 입력하면 해당 유저의 내부 PK(ID)를 반환합니다.")
    @PostMapping("/id/find")
    public BaseResponse<FindIdResponse> findIdByEmail(@Valid @RequestBody FindIdRequest req) {
        FindIdResponse response = userService.findUserId(req.getEmail());
        return BaseResponse.onSuccess(response);
    }

    @Operation(summary = "로그인 시 비밀번호 변경 후 전 디바이스 로그아웃", description = "비밀번호를 변경하고, 전 디바이스의 세션, 리멤버미 쿠키를 무효화합니다.")
    @PostMapping("/password/change")
    public BaseResponse<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest req,
                                             HttpServletRequest httpReq, HttpServletResponse httpRes) {
        AuthSessionUser principal = (AuthSessionUser) auth.getPrincipal();
        userService.changePassword(principal.id(), req);

        // 보안상 전 디바이스 만료
        logoutHelper.invalidateAllDevices(principal.username());
        // 현재 요청도 로그아웃 + 쿠키 만료
        logoutHelper.logoutCurrentRequest(httpReq, httpRes, auth, CookieProps.defaults());
        return BaseResponse.onSuccess(null);
    }
}
