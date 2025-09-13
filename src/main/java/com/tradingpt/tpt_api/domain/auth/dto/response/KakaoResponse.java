package com.tradingpt.tpt_api.domain.auth.dto.response;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    // 카카오에서 받은 전체 attributes 맵
    private final Map<String, Object> attributes;

    /**
     * attributes: 카카오에서 받은 사용자 정보 전체 맵
     */
    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 제공자명 반환 (kakao)
     */
    @Override
    public String getProvider() {
        return "KAKAO";
    }

    /**
     * 카카오 고유 사용자 ID 반환
     */
    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    /**
     * 카카오 계정 이메일 반환 (없을 수도도 있음)
     */
    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    /**
     * 카카오 프로필 닉네임 반환 (없을 수도 있음)
     */
    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                return (String) profile.get("nickname");
            }
        }
        return null;
    }
}
