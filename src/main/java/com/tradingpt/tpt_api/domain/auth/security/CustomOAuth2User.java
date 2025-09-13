package com.tradingpt.tpt_api.domain.auth.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User implements OAuth2User, Serializable {
    private final Long userId;
    private final String username;
    private final String role;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Long userId, String username, String role, Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.attributes = attributes;
    }

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getName() { return String.valueOf(userId); }
}
