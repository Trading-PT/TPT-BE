package com.tradingpt.tpt_api.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialInfoResponse {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String passwordHash; // 해시된 비밀번호 (원문 아님)
}
