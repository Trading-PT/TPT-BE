package com.tradingpt.tpt_api.global.config.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.Authentication;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class SecurityContextImplMixin {
    @JsonProperty("authentication")
    abstract Authentication getAuthentication();
    @JsonProperty("authentication")
    abstract void setAuthentication(Authentication authentication);
}
