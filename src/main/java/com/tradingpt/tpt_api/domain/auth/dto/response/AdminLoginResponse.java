package com.tradingpt.tpt_api.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponse {
    private Long id;
    private String username;
    private String role;
    private String name;
    private String email;
}
