package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.AfterCompletedGeneralSummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.AfterCompletedScalpingSummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.BeforeCompletedCourseSummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.EntryPointStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResult;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyPerformanceComparison;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.WeeklyFeedbackSummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyTradingSummaryQueryServiceImpl implements MonthlyTradingSummaryQueryService {

	private final CustomerRepository customerRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;

	@Override
	public YearlySummaryResponseDTO getYearlySummaryResponse(Integer year, Long customerId) {
		List<MonthlyFeedbackSummaryResult> monthlySummaries = feedbackRequestRepository
			.findMonthlySummaryByYear(customerId, year);

		List<YearlySummaryResponseDTO.MonthlyFeedbackSummaryDTO> months = monthlySummaries.stream()
			.map(YearlySummaryResponseDTO.MonthlyFeedbackSummaryDTO::of)
			.toList();

		return YearlySummaryResponseDTO.of(year, months);
	}

	@Override
	public List<MonthlySummaryResponseDTO> getMonthlySummaryResponse(Integer year, Integer month, Long customerId) {

		LocalDate date = LocalDate.of(year, month, 1);

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 1. 해당 월의 고유한 CourseStatus 목록 조회 (FeedbackRequest 생성 시점 기준)
		List<CourseStatus> courseStatuses = feedbackRequestRepository
			.findUniqueCourseStatusByYearMonth(customerId, year, month);

		if (courseStatuses.isEmpty()) {
			throw new UserException(UserErrorStatus.COURSE_STATUS_NOT_FOUND);
		}

		// 2. 해당 시점의 InvestmentType 조회
		InvestmentType investmentType = investmentTypeHistoryRepository.findActiveHistory(customerId, date)
			.orElseThrow(
				() -> new InvestmentHistoryException(InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 3. 각 CourseStatus별로 통계 생성 (완강 전 + 완강 후 모두 포함)
		return courseStatuses.stream()
			.map(courseStatus -> buildMonthlySummary(customerId, year, month, courseStatus, investmentType))
			.toList();
	}

	private MonthlySummaryResponseDTO buildMonthlySummary(Long customerId, Integer year, Integer month,
		CourseStatus courseStatus, InvestmentType investmentType) {

		// 공통 데이터 조회
		MonthlyFeedbackSummaryResponseDTO monthlyFeedbackSummary =
			feedbackRequestRepository.findMonthlyFeedbackSummaryByCourseStatus(customerId, year, month, courseStatus);

		MonthlyPerformanceComparison performanceComparison =
			feedbackRequestRepository.findMonthlyPerformanceComparison(customerId, year, month, courseStatus);

		if (courseStatus == CourseStatus.BEFORE_COMPLETION) {
			return BeforeCompletedCourseSummaryDTO.builder()
				.courseStatus(courseStatus)
				.investmentType(investmentType)
				.year(year)
				.month(month)
				.monthlyFeedbackSummaryResponseDTO(monthlyFeedbackSummary)
				.tradingPerformanceVariation(performanceComparison)
				.build();
		}

		if (courseStatus == CourseStatus.AFTER_COMPLETION && investmentType == InvestmentType.SCALPING) {
			// 스캘핑의 경우 주차별 통계를 임시로 빈 리스트로 처리 (실제 구현 시 수정 필요)
			List<WeeklyFeedbackSummaryDTO> weeklyFeedbacks = List.of();

			return AfterCompletedScalpingSummaryDTO.builder()
				.courseStatus(courseStatus)
				.investmentType(investmentType)
				.year(year)
				.month(month)
				.weeklyFeedbacks(weeklyFeedbacks)
				.build();
		}

		if (courseStatus == CourseStatus.AFTER_COMPLETION &&
			(investmentType == InvestmentType.SWING || investmentType == InvestmentType.DAY)) {

			EntryPointStatisticsResponseDTO entryPointStatistics =
				feedbackRequestRepository.findEntryPointStatisticsByCourseStatus(customerId, year, month, courseStatus,
					investmentType);

			// 월별 트레이딩 요약 조회 (트레이너 평가 정보)
			MonthlyTradingSummary monthlySummary = monthlyTradingSummaryRepository
				.findByCustomer_IdAndPeriodYearAndPeriodMonth(customerId, year, month)
				.orElse(null);

			boolean isTrainerEvaluated = monthlySummary != null;
			String monthlyEvaluation = isTrainerEvaluated ? monthlySummary.getMonthlyEvaluation() : null;
			String nextMonthGoal = isTrainerEvaluated ? monthlySummary.getNextMonthGoal() : null;

			return AfterCompletedGeneralSummaryDTO.builder()
				.courseStatus(courseStatus)
				.investmentType(investmentType)
				.year(year)
				.month(month)
				.monthlyFeedbackSummaryResponseDTO(monthlyFeedbackSummary)
				.isTrainerEvaluated(isTrainerEvaluated)
				.monthlyEvaluation(monthlyEvaluation)
				.nextMonthGoal(nextMonthGoal)
				.entryPointStatisticsResponseDTO(entryPointStatistics)
				.tradingPerformanceVariation(performanceComparison)
				.build();
		}

		throw new IllegalArgumentException("지원하지 않는 CourseStatus와 InvestmentType 조합입니다.");
	}

	private WeeklyFeedbackSummaryDTO convertToWeeklyFeedbackSummaryDTO(MonthlyWeekFeedbackSummaryResponseDTO dto) {
		return WeeklyFeedbackSummaryDTO.builder()
			.week(dto.getWeek())
			.totalCount(dto.getTradingCount())
			.hasUnreadFeedbackResponse(false) // 임시값, 실제로는 다른 DTO에서 가져와야 함
			.hasPendingTrainerResponse(false) // 임시값
			.build();
	}
}
