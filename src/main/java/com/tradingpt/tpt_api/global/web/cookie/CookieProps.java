package com.tradingpt.tpt_api.global.web.cookie;

import lombok.Builder;

@Builder
public record CookieProps(
        String path,        // 기본 "/"
        String domain,      // 필요 없으면 null
        String sameSite,    // "Lax" | "Strict" | "None" | null
        boolean secure      // 운영 HTTPS면 true
) {
    public static CookieProps defaults() {
        return CookieProps.builder()
                .path("/")
                .sameSite("Lax")
                .secure(false)
                .build();
    }
}
