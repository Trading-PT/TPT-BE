package com.tradingpt.tpt_api.domain.user.enums;

public enum UserStatus {
	UID_REVIEW_PENDING, // UID 검토 중
	UID_APPROVED, // UID 검토 완료, 승인
	UID_REJECTED, // UID 검토 완료, 거절
	TRAINER_ASSIGNED // 트레이너 배정 완료
}
