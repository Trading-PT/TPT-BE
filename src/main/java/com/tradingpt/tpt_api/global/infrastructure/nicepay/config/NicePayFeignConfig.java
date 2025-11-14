package com.tradingpt.tpt_api.global.infrastructure.nicepay.config;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayErrorDecoder;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.FormEncoder;

/**
 * NicePay Feign Client 설정
 * EUC-KR 인코딩 및 Form URL-encoded 요청/응답 지원
 */
@Configuration
public class NicePayFeignConfig {

	/**
	 * Feign 로거 레벨 설정
	 * 개발 환경에서는 FULL, 운영 환경에서는 BASIC 권장
	 */
	@Bean
	Logger.Level feignLoggerLevel(@Value("${nicepay.feign.logger-level}") String level) {
		return Logger.Level.valueOf(level);
	}

	/**
	 * NicePay API는 EUC-KR 인코딩을 사용하므로 커스텀 인코더 설정
	 * Form URL-encoded 요청을 위한 FormEncoder 사용
	 */
	@Bean
	public Encoder feignEncoder() {
		// EUC-KR 인코딩을 지원하는 StringHttpMessageConverter
		HttpMessageConverter<?> eucKrConverter = new StringHttpMessageConverter(
			Charset.forName("EUC-KR")
		);

		ObjectFactory<HttpMessageConverters> messageConverters =
			() -> new HttpMessageConverters(eucKrConverter);

		// FormEncoder로 감싸서 application/x-www-form-urlencoded 지원
		return new FormEncoder(new SpringEncoder(messageConverters));
	}

	/**
	 * NicePay API 응답은 Key=Value& 형식이므로 커스텀 디코더 사용
	 *
	 * 기존 SpringDecoder는 JSON/XML 등 표준 형식만 지원하므로
	 * URL-encoded 형식을 파싱할 수 있는 커스텀 Decoder 적용
	 */
	@Bean
	public Decoder feignDecoder() {
		return new NicePayFormDecoder();
	}

	/**
	 * NicePay API 에러 응답 처리를 위한 커스텀 ErrorDecoder
	 */
	@Bean
	public ErrorDecoder errorDecoder() {
		return new NicePayErrorDecoder();
	}

	/**
	 * Feign 재시도 정책
	 * NicePay API는 멱등성이 보장되지 않을 수 있으므로 재시도 비활성화
	 */
	@Bean
	public Retryer retryer() {
		return Retryer.NEVER_RETRY;
	}

	/**
	 * Feign 요청 옵션 설정
	 * 설정값은 application.yml에서 관리
	 */
	@Bean
	public Request.Options requestOptions(
		@Value("${nicepay.feign.connect-timeout}") int connectTimeout,
		@Value("${nicepay.feign.read-timeout}") int readTimeout,
		@Value("${nicepay.feign.follow-redirects}") boolean followRedirects
	) {
		return new Request.Options(
			connectTimeout, TimeUnit.SECONDS,
			readTimeout, TimeUnit.SECONDS,
			followRedirects
		);
	}
}