package com.tradingpt.tpt_api.domain.customermembershiphistory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MembershipStatus {

	ACTIVE("활성화 중"),
	PAUSED("일시 정지"),
	CANCELLED("취소"),
	INACTIVE("비활성화"),
	PENDING("연기"),
	;

	private final String description;
}
