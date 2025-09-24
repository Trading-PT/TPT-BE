package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

/**
 * FeedbackRequest 커스텀 Repository 인터페이스
 * QueryDSL을 사용한 동적 쿼리 메서드 정의
 */
public interface FeedbackRequestRepositoryCustom {

	/**
	 * 피드백 요청 목록을 동적 조건으로 페이징 조회
	 *
	 * @param pageable 페이징 정보
	 * @param feedbackType 피드백 타입 필터 (nullable)
	 * @param status 상태 필터 (nullable)
	 * @param customerId 고객 ID 필터 (nullable)
	 * @return 페이징된 피드백 요청 목록
	 */
	Page<FeedbackRequest> findFeedbackRequestsWithFilters(
		Pageable pageable,
		FeedbackType feedbackType,
		Status status,
		Long customerId
	);

	/**
	 * 특정 고객의 피드백 요청 목록을 동적 조건으로 조회
	 *
	 * @param customerId 고객 ID
	 * @param feedbackType 피드백 타입 필터 (nullable)
	 * @param status 상태 필터 (nullable)
	 * @return 피드백 요청 목록
	 */
	List<FeedbackRequest> findMyFeedbackRequests(
		Long customerId,
		FeedbackType feedbackType,
		Status status
	);

	/**
	 * 특정 고객이 해당 날짜에 작성한 데이 피드백 요청 개수를 반환한다.
	 *
	 * @param customerId 고객 ID
	 * @param feedbackDate 피드백 요청 날짜
	 * @return 해당 날짜의 요청 수
	 */
	long countDayRequestsByCustomerAndDate(Long customerId, LocalDate feedbackDate);
}
