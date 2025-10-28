package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.AdminFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestListItemResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlyPnlCalendarResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MyCustomerNewFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TokenUsedFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

public interface FeedbackRequestQueryService {

	/**
	 * 피드백 요청 목록 조회 (페이징)
	 */
	Page<FeedbackRequestResponseDTO> getFeedbackRequests(Pageable pageable, InvestmentType investmentType,
		Status status,
		Long customerId);

	/**
	 * 피드백 요청 상세 조회
	 */
	FeedbackRequestDetailResponseDTO getFeedbackRequestById(Long feedbackRequestId, Long currentUserId);

	/**
	 * 내 피드백 요청 목록 조회
	 */
	List<FeedbackRequestResponseDTO> getMyFeedbackRequests(Long customerId, InvestmentType investmentType,
		Status status);

	/**
	 * 모든 피드백 요청 목록 조회
	 *
	 * @param pageable 페이징 정보
	 * @return 피드백 리스트
	 */
	FeedbackListResponseDTO getFeedbackListSlice(Pageable pageable);

	/**
	 * 어드민 페이지
	 * 1. 현재 선정된 베스트 피드백
	 * 2. 전체 피드백 요청 목록
	 *
	 * @param pageable
	 * @return
	 */
	AdminFeedbackResponseDTO getAdminFeedbackListSlice(Pageable pageable);

	/**
	 * 특정 날짜의 피드백 요청 목록 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param day 일
	 * @return 해당 날짜의 피드백 요청 목록
	 */
	List<FeedbackRequestListItemResponseDTO> getDailyFeedbackRequests(
		Long customerId,
		Integer year,
		Integer month,
		Integer day
	);

	/**
	 * 월별 PnL 달력 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @return 월별 PnL 달력 데이터
	 */
	MonthlyPnlCalendarResponseDTO getMonthlyPnlCalendar(
		Long customerId,
		Integer year,
		Integer month
	);

	TokenUsedFeedbackListResponseDTO getTokenUsedFeedbackRequests(Pageable pageable);

	/**
	 * ✅ 내 담당 고객의 새로운 피드백 요청 목록 조회 (무한 스크롤)
	 *
	 * @param trainerId 트레이너 ID
	 * @param pageable 페이징 정보
	 * @return 새로운 피드백 요청 목록 (Slice)
	 */
	MyCustomerNewFeedbackListResponseDTO getMyCustomerNewFeedbackRequests(
		Long trainerId,
		Pageable pageable
	);

}
