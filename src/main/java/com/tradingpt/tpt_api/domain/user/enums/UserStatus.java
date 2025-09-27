package com.tradingpt.tpt_api.domain.user.enums;

public enum UserStatus {
    UID_REVIEW_PENDING, // UID 검토 중
    UID_APPROVED, // UID 검토 완료, 승인
    UID_REJECTED, // UID 검토 완료, 거절
    PAID_BEFORE_TEST, //유료 고객이며 레벨테스트 실시 전
    PAID_AFTER_TEST_TRAINER_ASSIGNING, //유료 고객이며 레벨테스트 완료해서 트레이너 배정 중
    RAINER_ASSIGNED // 트레이너 배정 완료


}
