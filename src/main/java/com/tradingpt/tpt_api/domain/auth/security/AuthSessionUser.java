package com.tradingpt.tpt_api.domain.auth.security;

import com.tradingpt.tpt_api.domain.user.entity.User;
import java.io.Serializable;

public record AuthSessionUser(
        Long id,
        String username,
        String role
) implements Serializable {
    public static AuthSessionUser fromUser(User user) {
        return new AuthSessionUser(user.getId(), user.getUsername(), user.getRole().name());
    }

    public static AuthSessionUser fromOAuth2User(CustomOAuth2User oAuth2User) {
        return new AuthSessionUser(oAuth2User.getUserId(), oAuth2User.getUsername(), oAuth2User.getRole());
    }
}

