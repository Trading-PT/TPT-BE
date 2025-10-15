package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.util.List;
import java.util.Optional;

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

	/**
	 * 해당 고객의 특정 연/월/주에 대한 첫 번째 피드백 조회 (생성 시간 오름차순)
	 * @param customerId
	 * @param feedbackYear
	 * @param feedbackMonth
	 * @param feedbackWeek
	 * @return
	 */
	Optional<FeedbackRequest> findFirstByCustomer_IdAndFeedbackYearAndFeedbackMonthAndFeedbackWeekOrderByCreatedAtAsc(
		Long customerId,
		Integer feedbackYear,
		Integer feedbackMonth,
		Integer feedbackWeek
	);

}
