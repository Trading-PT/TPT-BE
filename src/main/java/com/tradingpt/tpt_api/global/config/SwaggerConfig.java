package com.tradingpt.tpt_api.global.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfig {

	private static final String SESSION_SECURITY_SCHEME = "SessionCookie";
	private static final String CSRF_SECURITY_SCHEME = "CsrfHeader";
	private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
	private static final String DEFAULT_SERVER_DESCRIPTION = "기본 API 서버";

	private final SwaggerProperties swaggerProperties;

	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
			.title(swaggerProperties.getTitle())
			.version(swaggerProperties.getVersion())
			.description(swaggerProperties.getDescription())
			.license(new License()
				.name("Apache 2.0")
				.url("https://www.apache.org/licenses/LICENSE-2.0"));

		List<Server> servers = resolveServers();

		Components components = new Components()
			.addSecuritySchemes(SESSION_SECURITY_SCHEME, new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.COOKIE)
				.name("SESSION")
				.description("세션 로그인을 위해 발급받은 'SESSION' 쿠키를 전송합니다."))
			.addSecuritySchemes(CSRF_SECURITY_SCHEME, new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name("X-XSRF-TOKEN")
				.description("CSRF 보호를 위해 'XSRF-TOKEN' 쿠키 값을 이 헤더에 복사해 전송하세요."));

		SecurityRequirement requirement = new SecurityRequirement()
			.addList(SESSION_SECURITY_SCHEME)
			.addList(CSRF_SECURITY_SCHEME);

		return new OpenAPI()
			.info(info)
			.servers(servers)
			.components(components)
			.addSecurityItem(requirement);
	}

	private List<Server> resolveServers() {
		List<SwaggerProperties.Server> configuredServers = swaggerProperties.getServers();
		if (configuredServers == null || configuredServers.isEmpty()) {
			return List.of(new Server()
				.url(DEFAULT_SERVER_URL)
				.description(DEFAULT_SERVER_DESCRIPTION));
		}

		List<Server> servers = configuredServers.stream()
			.filter(Objects::nonNull)
			.filter(server -> StringUtils.hasText(server.getUrl()))
			.map(server -> new Server()
				.url(server.getUrl())
				.description(StringUtils.hasText(server.getDescription())
					? server.getDescription()
					: DEFAULT_SERVER_DESCRIPTION))
			.toList();

		return servers.isEmpty()
			? List.of(new Server().url(DEFAULT_SERVER_URL).description(DEFAULT_SERVER_DESCRIPTION))
			: Collections.unmodifiableList(servers);
	}
}
