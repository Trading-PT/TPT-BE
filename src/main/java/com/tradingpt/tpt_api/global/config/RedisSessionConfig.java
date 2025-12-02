package com.tradingpt.tpt_api.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.*;

import java.util.List;

@Configuration
@EnableRedisIndexedHttpSession( maxInactiveIntervalInSeconds = 86400,flushMode = FlushMode.IMMEDIATE)
public class RedisSessionConfig {

    private static final String ADMIN_PATH_PREFIX = "/api/v1/admin";

    /** Spring Session 전용: JDK 직렬화 사용 (SavedRequest 등 Jackson 이슈 방지) */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer(getClass().getClassLoader());
    }

    /**
     * 경로 기반으로 세션 쿠키명 분리:
     *  - /admin/**  -> ADMINSESSION (Path=/admin)
     *  - 그 외      -> SESSION      (Path=/)
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new PathAwareCookieSessionIdResolver("ADMINSESSION", "SESSION", ADMIN_PATH_PREFIX);
    }

    /** AWS Elasticache 등 일부 환경에서 필요한 설정 */
    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    /** 경로에 따라 다른 쿠키 시리얼라이저를 적용하는 커스텀 리졸버 */
    static class PathAwareCookieSessionIdResolver implements HttpSessionIdResolver {
        private final CookieHttpSessionIdResolver admin = new CookieHttpSessionIdResolver();
        private final CookieHttpSessionIdResolver user  = new CookieHttpSessionIdResolver();
        private final String adminPath;

        PathAwareCookieSessionIdResolver(String adminCookie, String userCookie, String adminPath) {
            this.adminPath = adminPath;

            DefaultCookieSerializer a = new DefaultCookieSerializer();
            a.setCookieName(adminCookie);
            a.setCookiePath(adminPath);
            a.setSameSite("None");
            a.setUseSecureCookie(true);
            a.setUseBase64Encoding(true);
            admin.setCookieSerializer(a);

            DefaultCookieSerializer u = new DefaultCookieSerializer();
            u.setCookieName(userCookie);
            u.setCookiePath("/");
            u.setSameSite("None");
            u.setUseSecureCookie(true);
            u.setUseBase64Encoding(true);
            user.setCookieSerializer(u);
        }

        private boolean isAdmin(HttpServletRequest req) {
            String p = req.getRequestURI();
            return p != null && p.startsWith(adminPath);
        }

        @Override public List<String> resolveSessionIds(HttpServletRequest req) {
            return isAdmin(req) ? admin.resolveSessionIds(req) : user.resolveSessionIds(req);
        }
        @Override public void setSessionId(HttpServletRequest req, HttpServletResponse res, String id) {
            if (isAdmin(req)) admin.setSessionId(req, res, id); else user.setSessionId(req, res, id);
        }
        @Override public void expireSession(HttpServletRequest req, HttpServletResponse res) {
            if (isAdmin(req)) admin.expireSession(req, res); else user.expireSession(req, res);
        }
    }
}
