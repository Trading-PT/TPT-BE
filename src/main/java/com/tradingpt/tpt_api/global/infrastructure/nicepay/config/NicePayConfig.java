package com.tradingpt.tpt_api.global.infrastructure.nicepay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * NicePay API 설정 클래스
 * application.yml의 nicepay 속성을 @Value로 주입받습니다.
 */
@Getter
@Configuration
public class NicePayConfig {

	// API URL Getter
	@Value("${nicepay.api.base-url}")
	private String apiBaseUrl;

	@Value("${nicepay.api.billing-register-path}")
	private String billingRegisterPath;

	@Value("${nicepay.api.billing-delete-path}")
	private String billingDeletePath;

	// Credentials Getter
	@Value("${nicepay.credentials.mid}")
	private String mid;

	@Value("${nicepay.credentials.merchant-key}")
	private String merchantKey;

	// Defaults Getter
	@Value("${nicepay.defaults.goods-name}")
	private String goodsName;

	@Value("${nicepay.defaults.amt}")
	private String amt;

}
