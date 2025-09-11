package com.tradingpt.tpt_api.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long userId;
    private String username;
    private String role;
}
