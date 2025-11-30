package com.tradingpt.tpt_api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class TradingPtApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingPtApplication.class, args);
	}

	/**
	 * JVM 기본 시간대를 Asia/Seoul(KST)로 설정
	 * 모든 LocalDateTime.now() 호출이 KST 기준으로 동작
	 */
	@PostConstruct
	public void initTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

}
