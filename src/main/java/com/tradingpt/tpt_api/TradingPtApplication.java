package com.tradingpt.tpt_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TradingPtApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingPtApplication.class, args);
	}

}
