package com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드백 요약 통계 베이스 클래스
 */
@Getter
@NoArgsConstructor   // ✅ 기본 생성자 추가
@AllArgsConstructor  // ✅ 모든 필드 생성자
public abstract class FeedbackSummaryBase {
	protected Long totalCount; // 총 개수
	protected Status status; // 읽음 상태
}
