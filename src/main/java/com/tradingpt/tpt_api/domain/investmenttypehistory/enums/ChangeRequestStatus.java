package com.tradingpt.tpt_api.domain.investmenttypehistory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 투자 유형 변경 신청 상태
 */
@Getter
@RequiredArgsConstructor
public enum ChangeRequestStatus {

	PENDING("대기중", "변경 신청이 승인 대기 중입니다."),
	APPROVED("승인됨", "변경 신청이 승인되었습니다."),
	REJECTED("거부됨", "변경 신청이 거부되었습니다."),
	CANCELLED("취소됨", "고객이 변경 신청을 취소했습니다.");

	private final String displayName;
	private final String description;
}