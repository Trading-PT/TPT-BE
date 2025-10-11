package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;

public interface FeedbackRequestRepository
	extends JpaRepository<FeedbackRequest, Long>, FeedbackRequestRepositoryCustom {

	/**
	 * 베스트 피드백 최대 3개 조회 (최신순)
	 * isBestFeedback = true인 피드백을 생성일시 기준 내림차순으로 최대 3개 반환
	 *
	 * @return 베스트 피드백 목록 (최대 3개)
	 */
	List<FeedbackRequest> findTop3ByIsBestFeedbackTrueOrderByCreatedAtDesc();

	/**
	 * 베스트 피드백 전체 조회
	 */
	List<FeedbackRequest> findByIsBestFeedbackTrue();

}
