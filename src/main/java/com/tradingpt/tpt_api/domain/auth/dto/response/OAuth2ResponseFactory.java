package com.tradingpt.tpt_api.domain.auth.dto.response;

import java.util.Map;

public final class OAuth2ResponseFactory {
    private OAuth2ResponseFactory() {}

    public static OAuth2Response fromAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("attributes is null");
        }

        // Kakao: 최상위에 "kakao_account" 키가 존재
        if (attributes.containsKey("kakao_account")) {
            return new KakaoResponse(attributes);
        }

        // Naver: 최상위에 "response" 맵이 존재
        Object resp = attributes.get("response");
        if (resp instanceof Map<?, ?>) {
            return new NaverResponse(attributes);
        }

        // 둘 다 아니면 미지원
        throw new IllegalArgumentException("Unsupported provider attributes: " + attributes.keySet());
    }
}
