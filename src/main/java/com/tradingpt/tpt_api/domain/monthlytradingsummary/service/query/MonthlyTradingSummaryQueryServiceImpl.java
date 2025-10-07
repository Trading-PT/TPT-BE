package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.EntryPointStatistics;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyPerformanceSnapshot;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.WeeklyRawData;
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

	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId) {

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 1. 해당 월의 CourseStatus 조회
		CourseStatus courseStatus = feedbackRequestRepository
			.findFirstByFeedbackYearAndFeedbackMonth(customerId, year, month)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND))
			.getCourseStatus();

		// 2. 해당 시점의 InvestmentType 조회
		InvestmentType investmentType = investmentTypeHistoryRepository.findActiveHistoryForMonth(customerId, year,
				month)
			.orElseThrow(
				() -> new InvestmentHistoryException(InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 3. CourseStatus에 따라 분기 처리
		if (courseStatus == CourseStatus.BEFORE_COMPLETION || courseStatus == CourseStatus.PENDING_COMPLETION) {
			return buildBeforeCompletionSummary(customerId, year, month, courseStatus, investmentType);
		} else {
			// 완강 후
			if (investmentType == InvestmentType.SCALPING) {
				return buildAfterCompletionScalpingSummary(customerId, year, month, courseStatus, investmentType);
			} else {
				return buildAfterCompletionGeneralSummary(customerId, year, month, courseStatus, investmentType);
			}
		}
	}

	/**
	 * 완강 전 월별 요약 생성
	 */
	private BeforeCompletedCourseSummaryDTO buildBeforeCompletionSummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 주차별 통계 조회
		List<WeeklyRawData> weeklyStats = feedbackRequestRepository.findWeeklyStatistics(
			customerId, year, month, courseStatus, investmentType
		);

		// 2. 주차별 DTO 변환 및 월간 통계 계산
		List<MonthlyWeekFeedbackSummaryResponseDTO> weekDTOs = weeklyStats.stream()
			.map(stat -> MonthlyWeekFeedbackSummaryResponseDTO.of(
				stat.getWeek(),
				stat.getTradingCount(),
				stat.getWeeklyPnl()
			))
			.toList();

		// 3. 월간 집계 계산
		Integer totalTradingCount = weeklyStats.stream()
			.mapToInt(WeeklyRawData::getTradingCount)
			.sum();

		Integer totalWinCount = weeklyStats.stream()
			.mapToInt(WeeklyRawData::getWinCount)
			.sum();

		BigDecimal monthlyPnl = weeklyStats.stream()
			.map(WeeklyRawData::getWeeklyPnl)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalRiskTaking = weeklyStats.stream()
			.map(WeeklyRawData::getTotalRiskTaking)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		Double winningRate = calculateWinningRate(totalTradingCount, totalWinCount);
		Double monthlyAverageRnr = calculateAverageRnR(monthlyPnl, totalRiskTaking);

		MonthlyFeedbackSummaryResponseDTO monthlyFeedback = MonthlyFeedbackSummaryResponseDTO.of(
			weekDTOs,
			winningRate,
			monthlyAverageRnr,
			monthlyPnl
		);

		// 4. 이전 달과 현재 달 성과 비교
		MonthlyPerformanceComparison performanceComparison = buildMonthlyPerformanceComparison(
			customerId, year, month, investmentType
		);

		return BeforeCompletedCourseSummaryDTO.of(
			courseStatus,
			investmentType,
			year,
			month,
			monthlyFeedback,
			performanceComparison
		);
	}

	/**
	 * 완강 후 일반 월별 요약 생성 (스윙/데이)
	 */
	private AfterCompletedGeneralSummaryDTO buildAfterCompletionGeneralSummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 주차별 통계 조회
		List<WeeklyRawData> weeklyStats = feedbackRequestRepository.findWeeklyStatistics(
			customerId, year, month, courseStatus, investmentType
		);

		// 2. 주차별 DTO 변환 및 월간 통계 계산
		List<MonthlyWeekFeedbackSummaryResponseDTO> weekDTOs = weeklyStats.stream()
			.map(stat -> MonthlyWeekFeedbackSummaryResponseDTO.of(
				stat.getWeek(),
				stat.getTradingCount(),
				stat.getWeeklyPnl()
			))
			.toList();

		Integer totalTradingCount = weeklyStats.stream()
			.mapToInt(WeeklyRawData::getTradingCount)
			.sum();

		Integer totalWinCount = weeklyStats.stream()
			.mapToInt(WeeklyRawData::getWinCount)
			.sum();

		BigDecimal monthlyPnl = weeklyStats.stream()
			.map(WeeklyRawData::getWeeklyPnl)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalRiskTaking = weeklyStats.stream()
			.map(WeeklyRawData::getTotalRiskTaking)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		Double winningRate = calculateWinningRate(totalTradingCount, totalWinCount);
		Double monthlyAverageRnr = calculateAverageRnR(monthlyPnl, totalRiskTaking);

		MonthlyFeedbackSummaryResponseDTO monthlyFeedback = MonthlyFeedbackSummaryResponseDTO.of(
			weekDTOs,
			winningRate,
			monthlyAverageRnr,
			monthlyPnl
		);

		// 3. 진입 타점 통계 조회
		EntryPointStatistics entryPointStats = feedbackRequestRepository.findEntryPointStatistics(
			customerId, year, month, investmentType
		);

		// 4. EntryPointStatisticsResponseDTO 변환 (lossCount → rnr)
		EntryPointStatisticsResponseDTO entryPointDTO = EntryPointStatisticsResponseDTO.of(
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getReverseCount(),
				entryPointStats.getReverseWinCount(),
				entryPointStats.getReverseRnr()  // 변경: lossCount → rnr
			),
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getPullBackCount(),
				entryPointStats.getPullBackWinCount(),
				entryPointStats.getPullBackRnr()  // 변경: lossCount → rnr
			),
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getBreakOutCount(),
				entryPointStats.getBreakOutWinCount(),
				entryPointStats.getBreakOutRnr()  // 변경: lossCount → rnr
			)
		);

		// 5. 트레이너 평가 조회
		Optional<MonthlyTradingSummary> evaluation = monthlyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_Month(customerId, year, month);

		Boolean isTrainerEvaluated = evaluation.isPresent();
		String monthlyEvaluation = evaluation.map(MonthlyTradingSummary::getMonthlyEvaluation).orElse(null);
		String nextMonthGoal = evaluation.map(MonthlyTradingSummary::getNextMonthGoal).orElse(null);

		// 6. 이전 달과 현재 달 성과 비교
		MonthlyPerformanceComparison performanceComparison = buildMonthlyPerformanceComparison(
			customerId, year, month, investmentType
		);

		return AfterCompletedGeneralSummaryDTO.of(
			monthlyFeedback,
			isTrainerEvaluated,
			monthlyEvaluation,
			nextMonthGoal,
			entryPointDTO,
			performanceComparison
		);
	}

	/**
	 * 완강 후 스캘핑 월별 요약 생성
	 */
	private AfterCompletedScalpingSummaryDTO buildAfterCompletionScalpingSummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 스캘핑은 주차별 요약만 제공
		List<MonthlyFeedbackSummaryResult> weeklySummaries =
			feedbackRequestRepository.findMonthlySummaryByYear(customerId, year)
				.stream()
				.filter(result -> result.month().equals(month))
				.toList();

		// 주차별로 그룹핑하여 WeeklyFeedbackSummaryDTO 생성
		// 주의: findMonthlySummaryByYear는 월별 요약이므로, 주차별 조회를 위한 별도 로직이 필요할 수 있음
		// 여기서는 간단히 기존 메서드를 활용하되, 실제로는 주차별 조회 메서드가 필요할 수 있습니다.

		List<WeeklyFeedbackSummaryDTO> weeklyFeedbacks = feedbackRequestRepository
			.findWeeklyStatistics(customerId, year, month, courseStatus, investmentType)
			.stream()
			.map(stat -> {
				// 각 주차별로 읽지 않은 답변 및 대기 중 요청 확인 로직
				// 실제 구현 시 Status를 확인하는 로직 추가 필요
				Boolean hasUnreadFeedbackResponse = false; // TODO: 실제 조회 로직
				Boolean hasPendingTrainerResponse = false; // TODO: 실제 조회 로직

				return WeeklyFeedbackSummaryDTO.of(
					stat.getWeek(),
					stat.getTradingCount(),
					hasUnreadFeedbackResponse,
					hasPendingTrainerResponse
				);
			})
			.toList();

		return AfterCompletedScalpingSummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.weeklyFeedbacks(weeklyFeedbacks)
			.build();
	}

	/**
	 * 월별 성과 비교 생성 (이전 달 vs 현재 달)
	 */
	private MonthlyPerformanceComparison buildMonthlyPerformanceComparison(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		// 현재 달 성과
		MonthlyPerformanceSnapshot currentSnapshot = feedbackRequestRepository.findMonthlyPerformance(
			customerId, year, month, investmentType
		);

		// 이전 달 계산
		LocalDate currentDate = LocalDate.of(year, month, 1);
		LocalDate previousDate = currentDate.minusMonths(1);
		Integer previousYear = previousDate.getYear();
		Integer previousMonth = previousDate.getMonthValue();

		// 이전 달 성과
		MonthlyPerformanceSnapshot previousSnapshot = feedbackRequestRepository.findMonthlyPerformance(
			customerId, previousYear, previousMonth, investmentType
		);

		MonthlyPerformanceComparison.MonthSnapshot beforeMonth = MonthlyPerformanceComparison.MonthSnapshot.of(
			previousMonth,
			previousSnapshot.getFinalWinRate(),
			previousSnapshot.getAverageRnr(),
			previousSnapshot.getFinalPnl()
		);

		MonthlyPerformanceComparison.MonthSnapshot currentMonthSnapshot = MonthlyPerformanceComparison.MonthSnapshot.of(
			month,
			currentSnapshot.getFinalWinRate(),
			currentSnapshot.getAverageRnr(),
			currentSnapshot.getFinalPnl()
		);

		return MonthlyPerformanceComparison.of(beforeMonth, currentMonthSnapshot);
	}

	/**
	 * 승률 계산
	 * 공식: (수익 횟수 / 매매 횟수) × 100
	 */
	private Double calculateWinningRate(Integer totalCount, Integer winCount) {
		if (totalCount == null || totalCount == 0) {
			return 0.0;
		}
		return BigDecimal.valueOf(winCount)
			.divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100))
			.setScale(2, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * 평균 R&R 계산
	 * 공식: P&L / 리스크 테이킹
	 */
	private Double calculateAverageRnR(BigDecimal totalPnl, BigDecimal totalRiskTaking) {
		if (totalRiskTaking == null || totalRiskTaking.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}
		return totalPnl.divide(totalRiskTaking, 2, RoundingMode.HALF_UP).doubleValue();
	}

}
