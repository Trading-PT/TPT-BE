package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.DailyPnlProjection;
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
	 * 특정 고객이 특정 날짜에 생성한 모든 피드백 요청을 조회한다.
	 *
	 * @param customerId   고객 ID
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
	 * @param customerId     고객 ID
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
	 *
	 * @param customerId
	 * @param year
	 * @param month
	 * @return
	 */
	Optional<FeedbackRequest> findFirstByFeedbackYearAndFeedbackMonth(Long customerId, Integer year, Integer month);

	/**
	 * 주간 일별 통계 조회
	 *
	 * @param customerId     고객 ID
	 * @param year           연도
	 * @param month          월
	 * @param week           주차
	 * @param courseStatus   완강 여부
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
	 * @param customerId     고객 ID
	 * @param year           연도
	 * @param month          월
	 * @param week           주차
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
	 * @param year       연도
	 * @param month      월
	 * @param week       주차
	 * @return 방향성 통계
	 */
	DirectionStatistics findDirectionStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);

	/**
	 * 해당 고객의 특정 연/월에 특정 CourseStatus를 가진 피드백이 존재하는지 확인
	 *
	 * @param customerId   고객 ID
	 * @param year         연도
	 * @param month        월
	 * @param courseStatus 코스 상태
	 * @return 존재하면 true, 없으면 false
	 */
	boolean existsByCustomerIdAndYearAndMonthAndCourseStatus(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus
	);

	/**
	 * 특정 날짜의 고객 피드백 요청 목록 조회 (시간 순 정렬)
	 * feedbackRequestDate를 사용하여 날짜 필터링
	 *
	 * @param customerId 고객 ID
	 * @param targetDate 조회할 날짜
	 * @return 해당 날짜의 피드백 요청 목록 (작성 시간 오름차순)
	 */
	List<FeedbackRequest> findByCustomerIdAndDate(
		Long customerId,
		LocalDate targetDate
	);

	/**
	 * 특정 연/월의 모든 피드백 요청 조회 (날짜별로 그룹핑하여 PnL 합산)
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @return 날짜별 PnL 프로젝션 리스트
	 */
	List<DailyPnlProjection> findDailyPnlByCustomerIdAndYearAndMonth(
		Long customerId,
		Integer year,
		Integer month
	);

	/**
	 * ✅ 토큰 사용 피드백 요청 목록 조회 (무한 스크롤)
	 *
	 * @param pageable 페이징 정보
	 * @return 토큰 사용 피드백 요청 Slice
	 */
	Slice<FeedbackRequest> findTokenUsedFeedbackRequests(Pageable pageable);

	/**
	 * ✅ 특정 트레이너의 담당 고객들의 새로운 피드백 요청 목록 조회 (무한 스크롤)
	 * - status가 N (피드백 대기)인 것만 조회
	 * - 최신순 정렬
	 *
	 * @param trainerId 트레이너 ID
	 * @param pageable 페이징 정보
	 * @return 새로운 피드백 요청 Slice
	 */
	Slice<FeedbackRequest> findNewFeedbackRequestsByTrainer(
		Long trainerId,
		Pageable pageable
	);

	/**
	 * ✅ 모든 고객의 새로운 프리미엄 피드백 요청 목록 조회 (무한 스크롤) - ADMIN 전용
	 * - status가 N (피드백 대기)인 것만 조회
	 * - isTokenUsed가 true인 것만 조회
	 * - membershipLevel이 PREMIUM인 것만 조회
	 * - 최신순 정렬
	 *
	 * @param pageable 페이징 정보
	 * @return 새로운 프리미엄 피드백 요청 Slice
	 */
	Slice<FeedbackRequest> findAllNewPremiumFeedbackRequests(Pageable pageable);

	/**
	 * ✅ 특정 고객의 특정 연/월에 피드백 요청이 존재하는 주차 목록 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @return 피드백이 존재하는 주차 목록 (오름차순 정렬)
	 */
	List<Integer> findWeeksByCustomerIdAndYearAndMonth(
		Long customerId,
		Integer year,
		Integer month
	);

	/**
	 * ✅ 특정 고객의 특정 연/월/주에 피드백 요청이 존재하는 날짜(일) 목록 조회
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 피드백이 존재하는 날짜 목록 (오름차순 정렬)
	 */
	List<Integer> findDaysByCustomerIdAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);

	/**
	 * ✅ 특정 고객의 특정 연/월/주에 대한 이익 매매 피드백 목록 조회
	 * - courseStatus = BEFORE_COMPLETION인 것만 조회
	 * - pnl > 0인 것만 조회 (이익)
	 * - feedbackRequestDate 기준 내림차순 정렬
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 이익 매매 피드백 목록
	 */
	List<FeedbackRequest> findProfitFeedbacksByCustomerAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);

	/**
	 * ✅ 특정 고객의 특정 연/월/주에 대한 손실 매매 피드백 목록 조회
	 * - courseStatus = BEFORE_COMPLETION인 것만 조회
	 * - pnl < 0인 것만 조회 (손실)
	 * - feedbackRequestDate 기준 내림차순 정렬
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 손실 매매 피드백 목록
	 */
	List<FeedbackRequest> findLossFeedbacksByCustomerAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);

	/**
	 * ✅ 트레이너가 직접 작성한 매매일지 목록 조회 (무한 스크롤)
	 * - isTrainerWritten = true인 것만 조회
	 * - 최신순 정렬 (createdAt DESC)
	 * - 첨부 이미지 포함
	 *
	 * @param pageable 페이징 정보
	 * @return 트레이너 작성 매매일지 Slice
	 */
	Slice<FeedbackRequest> findTrainerWrittenFeedbacks(Pageable pageable);

	/**
	 * ✅ 주간 매매일지의 수익 매매 평균 R&R 조회
	 * - 특정 주차의 pnl > 0인 수익 매매만 조회
	 * - DB의 rnr 컬럼 합계 / 수익 매매 개수로 계산
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param courseStatus 완강 여부
	 * @param investmentType 투자 타입
	 * @return 수익 매매의 평균 R&R (소수점 2자리)
	 */
	Double findAverageRnRForWeeklySummary(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	);

	/**
	 * ✅ 월간 매매일지의 수익 매매 평균 R&R 조회
	 * - pnl > 0인 수익 매매만 조회
	 * - DB의 rnr 컬럼 합계 / 수익 매매 개수로 계산
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param investmentType 투자 타입
	 * @return 수익 매매의 평균 R&R (소수점 2자리)
	 */
	Double findAverageRnRForMonthlySummary(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	);

	/**
	 * ✅ 주간 성과 비교용 수익 매매 평균 R&R 조회
	 * - 특정 주차의 pnl > 0인 수익 매매만 조회
	 * - DB의 rnr 컬럼 합계 / 수익 매매 개수로 계산
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param investmentType 투자 타입
	 * @return 수익 매매의 평균 R&R (소수점 2자리)
	 */
	Double findAverageRnRForWeeklyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	);

}
