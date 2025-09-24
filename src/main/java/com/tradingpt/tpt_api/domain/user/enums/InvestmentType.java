package com.tradingpt.tpt_api.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvestmentType {
	SWING("스윙"),
	DAY("데이"),
	SCALPING("스켈핑"),
	;

	private final String description;
}
