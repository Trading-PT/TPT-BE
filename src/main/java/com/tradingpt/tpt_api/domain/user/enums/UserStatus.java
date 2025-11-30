package com.tradingpt.tpt_api.domain.user.enums;

public enum UserStatus {
	UID_REVIEW_PENDING, // UID 검토 중
	UID_APPROVED, // UID 검토 완료, 승인
	UID_REJECTED, // UID 검토 완료, 거절
	PAID_BEFORE_TRAINER_ASSIGNING, // 결제 완료, 트레이너 배정 전
	TRAINER_ASSIGNED // 트레이너 배정 완료
}
