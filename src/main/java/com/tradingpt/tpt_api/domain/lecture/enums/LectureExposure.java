package com.tradingpt.tpt_api.domain.lecture.enums;

/**
 * 강의 공개 범위 및 방식(Enum)
 *
 * - SUBSCRIBER_WEEKLY : 구독 고객에게 주차별 공개
 * - PUBLIC_INSTANT     : 모든 고객에게 즉시 공개 (무료 강의)
 * - PRIVATE             : 비공개 (트레이너/관리자만 접근 가능)
 */
public enum LectureExposure {
    SUBSCRIBER_WEEKLY,  // 구독 고객 대상, 주차별 순차 공개
    PUBLIC_INSTANT,     // 전체 무료 즉시 공개
    PRIVATE             // 비공개
}
