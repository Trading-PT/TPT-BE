package com.tradingpt.tpt_api.domain.auth.repository;

import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.cookie.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2AuthorizationRequest를 세션 대신 쿠키에 싣는 저장소
 * - Java Serialization + Base64 인코딩 사용 (Spring Security 클래스는 Jackson 직렬화 미지원)
 * - 장점: Redis 세션에 복잡한 객체가 안 들어감
 * - 주의: 쿠키 사이즈 제한(보통 4KB) 내에서 동작
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQ";
    private static final int EXPIRE_SECONDS = (int) Duration.ofMinutes(5).toSeconds();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        log.info("[OAuth2 Cookie] loadAuthorizationRequest - cookie 존재: {}", cookie != null);

        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            log.warn("[OAuth2 Cookie] 쿠키가 없거나 값이 비어있음! 요청 URL: {}", request.getRequestURI());
            return null;
        }
        try {
            OAuth2AuthorizationRequest authRequest = deserialize(cookie.getValue());
            log.info("[OAuth2 Cookie] 쿠키에서 AuthorizationRequest 로드 성공 - state: {}", authRequest.getState());
            return authRequest;
        } catch (Exception e) {
            log.error("[OAuth2 Cookie] 쿠키 파싱 실패: {}", e.getMessage());
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
            String serialized = serialize(authorizationRequest);

            // HTTPS 환경 감지 (X-Forwarded-Proto 헤더 또는 request.isSecure())
            boolean isSecure = request.isSecure()
                    || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

            log.info("[OAuth2 Cookie] saveAuthorizationRequest - state: {}, isSecure: {}, cookieSize: {}",
                    authorizationRequest.getState(), isSecure, serialized.length());

            Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(EXPIRE_SECONDS);
            cookie.setSecure(isSecure);  // HTTPS 환경에서는 true
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("[OAuth2 Cookie] 쿠키 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * OAuth2AuthorizationRequest를 Base64 문자열로 직렬화
     */
    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(authorizationRequest);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new IllegalArgumentException("OAuth2AuthorizationRequest 직렬화 실패", e);
        }
    }

    /**
     * Base64 문자열에서 OAuth2AuthorizationRequest로 역직렬화
     */
    private OAuth2AuthorizationRequest deserialize(String serialized) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(serialized);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (OAuth2AuthorizationRequest) ois.readObject();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("OAuth2AuthorizationRequest 역직렬화 실패", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return req;
    }

    /** 인가요청 관련 쿠키 일괄 제거 (CookieUtils 사용) */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        // 이 프로젝트의 기본 쿠키 속성으로 만료
        CookieProps props = CookieProps.defaults();
        CookieUtils.expire(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, true, props);
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
