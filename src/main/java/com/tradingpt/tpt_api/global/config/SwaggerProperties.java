package com.tradingpt.tpt_api.global.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.swagger")
public class SwaggerProperties {

    private String title = "Trading PT API";
    private String description = "Trading Platform API 문서";
    private String version = "1.0";
    private List<Server> servers = new ArrayList<>();

    @Getter
    @Setter
    public static class Server {
        private String url;
        private String description;
    }
}
