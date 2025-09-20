package com.tradingpt.tpt_api.global.config.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * UsernamePasswordAuthenticationToken 역직렬화용 MixIn.
 * - authenticated 필드 바인딩을 무시해서 "Cannot set this token to trusted" 예외 방지
 * - principal, credentials, authorities를 생성자에서만 처리
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonIgnoreProperties(value = { "authenticated" }, allowSetters = false, ignoreUnknown = true)
public abstract class UsernamePasswordAuthenticationTokenMixin {
    @JsonCreator
    public UsernamePasswordAuthenticationTokenMixin(
            @JsonProperty("principal") Object principal,
            @JsonProperty("credentials") Object credentials,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) { }

    @JsonProperty("details")
    abstract Object getDetails();

    @JsonProperty("details")
    abstract void setDetails(Object details);
}

