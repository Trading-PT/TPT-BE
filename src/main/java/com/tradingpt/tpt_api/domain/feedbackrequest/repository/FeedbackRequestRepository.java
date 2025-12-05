package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;

public interface FeedbackRequestRepository
	extends JpaRepository<FeedbackRequest, Long>, FeedbackRequestRepositoryCustom {

	/**
	 * 베스트 피드백 조회 (최신순, 개수 제한은 Pageable로 제어)
	 * isBestFeedback = true인 피드백을 생성일시 기준 내림차순으로 반환
	 *
	 * @param pageable 페이징 정보 (PageRequest.of(0, MAX_BEST_FEEDBACK_COUNT) 사용)
	 * @return 베스트 피드백 목록
	 * @see FeedbackRequest#MAX_BEST_FEEDBACK_COUNT
	 */
	List<FeedbackRequest> findByIsBestFeedbackTrueOrderByCreatedAtDesc(Pageable pageable);

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

	/**
	 * 특정 고객의 피드백 요청 총 개수 조회
	 * 토큰 보상 시스템의 정합성 검증용
	 *
	 * @param customerId 고객 ID
	 * @return 해당 고객의 피드백 요청 개수
	 */
	long countByCustomer_Id(Long customerId);

}
