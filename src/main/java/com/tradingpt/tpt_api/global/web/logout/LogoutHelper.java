package com.tradingpt.tpt_api.global.web.logout;

import com.tradingpt.tpt_api.domain.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.tradingpt.tpt_api.global.web.cookie.CookieProps;
import com.tradingpt.tpt_api.global.web.cookie.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutHelper {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final ObjectProvider<PersistentTokenRepository> persistentTokenRepositoryProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository oauth2CookieRepo;
    private final StringRedisTemplate redisTemplate;

    /**
     * 현재 요청만 로그아웃 (단일 디바이스)
     * - Redis 세션 완전 삭제 (session + expires)
     * - 브라우저 쿠키 만료
     */
    public void logoutCurrentRequest(HttpServletRequest req, HttpServletResponse res,
                                     Authentication auth, CookieProps cookieProps) {

        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            String id = httpSession.getId();
            String sessionKey = "spring:session:sessions:" + id;
            String expiresKey = "spring:session:sessions:expires:" + id;

            // Spring Session 삭제
            sessionRepository.deleteById(id);

            // 혹시 남은 Redis 키 강제 제거 (flushMode 꼬임 방지)
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(expiresKey);

            // 세션 무효화
            httpSession.invalidate();
        }

        // SecurityContext 정리
        new SecurityContextLogoutHandler().logout(req, res, auth);

        // 브라우저 쿠키 제거 (SESSION, REMEMBER_ME 등)
        CookieUtils.expireAuthCookies(res, cookieProps);

        //  OAuth2 인가 요청 쿠키 제거
        oauth2CookieRepo.removeAuthorizationRequestCookies(req, res);

        log.info("[LogoutHelper] Current device logged out and session invalidated.");
    }

    /**
     *  모든 디바이스 로그아웃 (회원탈퇴, 비밀번호 변경 시)
     * - username 인덱스로 Redis 세션 전체 삭제
     * - persistent_logins 테이블(remember-me) 정리
     */
    public void invalidateAllDevices(String username) {
        Map<String, ? extends Session> sessions =
                sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username);

        sessions.keySet().forEach(id -> {
            String sessionKey = "spring:session:sessions:" + id;
            String expiresKey = "spring:session:sessions:expires:" + id;

            // Spring Session 삭제
            sessionRepository.deleteById(id);

            redisTemplate.delete(sessionKey);
            redisTemplate.delete(expiresKey);
        });

        // remember-me 토큰 삭제
        persistentTokenRepositoryProvider.ifAvailable(repo -> repo.removeUserTokens(username));

    }
}
