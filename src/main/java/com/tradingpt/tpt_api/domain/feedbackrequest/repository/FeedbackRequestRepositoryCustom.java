package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.EntryPointStatistics;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyFeedbackSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyPerformanceSnapshot;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.WeeklyRawData;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DailyRawData;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DirectionStatistics;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.WeeklyPerformanceSnapshot;

/**
 * FeedbackRequest 커스텀 Repository 인터페이스
 * QueryDSL을 사용한 동적 쿼리 메서드 정의
 */
public interface FeedbackRequestRepositoryCustom {

	/**
	 * 피드백 요청 목록을 동적 조건으로 페이징 조회
	 *
	 * @param pageable 페이징 정보
	 * @param investmentType 투자 유형 필터 (nullable)
	 * @param status 상태 필터 (nullable)
	 * @param customerId 고객 ID 필터 (nullable)
	 * @return 페이징된 피드백 요청 목록
	 */
	Page<FeedbackRequest> findFeedbackRequestsWithFilters(
		Pageable pageable,
		InvestmentType investmentType,
		Status status,
		Long customerId
	);

	/**
	 * 모든 피드백 요청 목록 조회 (무한 스크롤)
	 * 정렬: isBestFeedback DESC, createdAt DESC
	 *
	 * @param pageable 페이징 정보
	 * @return 피드백 요청 Slice
	 */
	Slice<FeedbackRequest> findAllFeedbackRequestsSlice(Pageable pageable);

	/**
	 * 모든 피드백 조회 (무한 스크롤 - 단순 최신순)
	 * 베스트 우선 정렬 없이 순수하게 최신순만 정렬
	 *
	 * @param pageable 페이징 정보
	 * @return 피드백 Slice
	 */
	Slice<FeedbackRequest> findAllFeedbacksByCreatedAtDesc(Pageable pageable);

	/**
	 * 특정 고객의 피드백 요청 목록을 동적 조건으로 조회
	 *
	 * @param customerId 고객 ID
	 * @param investmentType 투자 유형 필터 (nullable)
	 * @param status 상태 필터 (nullable)
	 * @return 피드백 요청 목록
	 */
	List<FeedbackRequest> findMyFeedbackRequests(
		Long customerId,
		InvestmentType investmentType,
		Status status
	);

	/**
	 * 특정 고객이 특정 날짜에 생성한 모든 피드백 요청을 조회한다.
	 *
	 * @param customerId 고객 ID
	 * @param feedbackDate 피드백 요청 날짜
	 * @return 해당 날짜의 피드백 요청 목록 (생성 시간 오름차순)
	 */
	List<FeedbackRequest> findFeedbackRequestsByCustomerAndDate(Long customerId, LocalDate feedbackDate);

	/**
	 * 특정 연도에 대해 고객의 월별 피드백 요약 정보를 조회한다.
	 *
	 * @param customerId 고객 ID
	 * @param year       조회 연도
	 * @return 월별 요청 수/읽지 않은 답변/답변 대기 여부를 포함한 요약 결과
	 */
	List<MonthlyFeedbackSummary> findMonthlySummaryByYear(Long customerId, Integer year);

	/**
	 * 특정 고객이 해당 날짜에 작성한 데이 피드백 요청 개수를 반환한다.
	 *
	 * @param customerId 고객 ID
	 * @param investmentType 피드백 요청 날짜
	 * @return 해당 날짜의 요청 수
	 */
	long countRequestsByCustomerAndDateAndType(
		Long customerId,
		LocalDate feedbackDate,
		InvestmentType investmentType
	);

	/**
	 * 특정 고객의 특정 연/월/CourseStatus별 주차 통계 조회
	 */
	List<WeeklyRawData> findWeeklyStatistics(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	);

	/**
	 * 진입 타점별 통계 조회 (완강 후, 스윙/데이만)
	 */
	EntryPointStatistics findEntryPointStatistics(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	);

	/**
	 * 특정 달의 최종 성과 조회
	 */
	MonthlyPerformanceSnapshot findMonthlyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	);

	/**
	 * 고객의 ID와 특정 연/월에 대해서 가장 최근의 FeedbackRequest를 조회
	 * @param customerId
	 * @param year
	 * @param month
	 * @return
	 */
	Optional<FeedbackRequest> findFirstByFeedbackYearAndFeedbackMonth(Long customerId, Integer year, Integer month);

	/**
	 * 주간 일별 통계 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param courseStatus 완강 여부
	 * @param investmentType 투자 타입
	 * @return 일별 원시 데이터 리스트
	 */
	List<DailyRawData> findDailyStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	);

	/**
	 * 주간 성과 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param investmentType 투자 타입
	 * @return 주간 성과 스냅샷
	 */
	WeeklyPerformanceSnapshot findWeeklyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	);

	/**
	 * 데이 트레이딩 방향성 통계 조회 (완강 후)
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 방향성 통계
	 */
	DirectionStatistics findDirectionStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);

}
