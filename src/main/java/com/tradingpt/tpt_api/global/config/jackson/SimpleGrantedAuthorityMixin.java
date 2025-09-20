package com.tradingpt.tpt_api.global.config.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SimpleGrantedAuthorityMixin {
    @JsonCreator
    public SimpleGrantedAuthorityMixin(@JsonProperty("authority") String authority) { }
}
