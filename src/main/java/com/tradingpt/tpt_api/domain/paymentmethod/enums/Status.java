package com.tradingpt.tpt_api.domain.paymentmethod.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	PENDING("대기 중"),
	COMPLETED("완료"),
	FAILED("실패"),
	;
	
	private final String description;
}
