package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

/**
 * 연도/고객별 월간 피드백 요약 조회 결과.
 */
public record MonthlyFeedbackSummaryResult(
	Integer month,
	Long totalCount,
	Integer unreadCount,
	Integer pendingCount
) {
}
