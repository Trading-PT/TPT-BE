// src/main/java/.../auth/repository/HttpCookieOAuth2AuthorizationRequestRepository.java
package com.tradingpt.tpt_api.domain.auth.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.util.StringUtils;

/**
 * OAuth2AuthorizationRequest를 세션 대신 쿠키에 싣는 저장소
 * - 장점: Redis 세션에 복잡한 객체가 안 들어감
 * - 주의: 쿠키 사이즈 제한(보통 4KB) 내에서 동작
 */
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQ";
    private static final int EXPIRE_SECONDS = (int) Duration.ofMinutes(5).toSeconds();

    private final ObjectMapper objectMapper;

    public HttpCookieOAuth2AuthorizationRequestRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) return null;
        try {
            String json = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, OAuth2AuthorizationRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(authorizationRequest);
            String enc = URLEncoder.encode(json, StandardCharsets.UTF_8);
            Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, enc);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(EXPIRE_SECONDS);
            cookie.setSecure(false); // HTTPS면 true 권장
            response.addCookie(cookie);
        } catch (Exception ignored) {}
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return req;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (cookie != null) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    private Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }
}
