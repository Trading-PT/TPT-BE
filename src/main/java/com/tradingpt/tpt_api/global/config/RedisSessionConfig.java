package com.tradingpt.tpt_api.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.tradingpt.tpt_api.global.config.jackson.SecurityContextImplMixin;
import com.tradingpt.tpt_api.global.config.jackson.UsernamePasswordAuthenticationTokenMixin;
import com.tradingpt.tpt_api.global.config.jackson.SimpleGrantedAuthorityMixin;
import com.tradingpt.tpt_api.global.config.jackson.WebAuthenticationDetailsMixin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableRedisIndexedHttpSession
public class RedisSessionConfig {

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.")
                .allowIfSubType("org.springframework.")
                .allowIfSubType("com.tradingpt.")
                .build();

        ObjectMapper om = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();

        // 필요 시 유지/제거 가능 (Security 타입엔 아래 MixIn의 @JsonTypeInfo가 붙음)
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // public MixIn 등록
        om.addMixIn(SecurityContextImpl.class, SecurityContextImplMixin.class);
        om.addMixIn(UsernamePasswordAuthenticationToken.class, UsernamePasswordAuthenticationTokenMixin.class);
        om.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class);
        om.addMixIn(WebAuthenticationDetails.class, WebAuthenticationDetailsMixin.class);

        return new GenericJackson2JsonRedisSerializer(om);
    }

    @Bean
    public DefaultCookieSerializer defaultCookieSerializer() {
        DefaultCookieSerializer s = new DefaultCookieSerializer();
        s.setCookieName("SESSION");
        s.setCookiePath("/");
        s.setUseBase64Encoding(true);
        s.setSameSite("Lax");      // 크로스 도메인이면 "None" + Secure=true
        s.setUseSecureCookie(false); // 운영은 true
        return s;
    }

    @Bean
    public ConfigureRedisAction configureRedisAction() {
        // Spring Session이 CONFIG GET/SET을 전혀 시도하지 않게 함
        return ConfigureRedisAction.NO_OP;
    }
}
