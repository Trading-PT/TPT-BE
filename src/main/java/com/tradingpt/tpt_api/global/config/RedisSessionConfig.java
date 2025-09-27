package com.tradingpt.tpt_api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableRedisIndexedHttpSession(flushMode = FlushMode.IMMEDIATE)
public class RedisSessionConfig {

    /** Spring Session 전용: JDK 직렬화 사용 (DefaultSavedRequest 등 Jackson 이슈 방지) */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer(getClass().getClassLoader());
    }

    @Bean
    public DefaultCookieSerializer defaultCookieSerializer() {
        DefaultCookieSerializer s = new DefaultCookieSerializer();
        s.setCookieName("SESSION");
        s.setCookiePath("/");
        s.setUseBase64Encoding(true);
        s.setSameSite("None");      // ★ 리다이렉트/크로스-사이트 대응
        s.setUseSecureCookie(true);
        return s;
    }

    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
