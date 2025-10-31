// src/main/java/com/tradingpt/tpt_api/domain/auth/controller/AdminAuthController.java
package com.tradingpt.tpt_api.domain.auth.controller;

import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.logout.LogoutHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "어드민 인증", description = "어드민 인증/인가 관련 API")
public class AdminAuthController {

    private final LogoutHelper logoutHelper;

    @Operation(summary = "어드민 로그아웃",
            description = "현재 디바이스의 ADMIN 세션/리멤버미/레디스 세션을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest req,
                                                     HttpServletResponse res,
                                                     Authentication auth) {
        logoutHelper.logoutCurrentRequest(req, res, auth, CookieProps.defaults());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(null));
    }
}
