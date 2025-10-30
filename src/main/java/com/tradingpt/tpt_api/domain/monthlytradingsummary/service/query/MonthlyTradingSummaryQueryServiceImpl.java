package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.DateValidationUtil;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackStatusUtil;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.TradingCalculationUtil;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.EntryPointStatistics;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyFeedbackSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyPerformanceSnapshot;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.WeeklyRawData;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.AfterCompletedGeneralMonthlySummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.AfterCompletedScalpingMonthlySummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.BeforeCompletedCourseMonthlySummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.EntryPointStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.PerformanceComparison;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.WeeklyFeedbackSummaryDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;

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
	private final TrainerRepository trainerRepository;

	@Override
	public YearlySummaryResponseDTO getYearlySummaryResponse(Integer year, Long customerId) {

		// 연도 검증 ( 올해 or 과거만 가능 )
		DateValidationUtil.validatePastOrPresentYear(year);

		List<MonthlyFeedbackSummary> monthlySummaries = feedbackRequestRepository
			.findMonthlySummaryByYear(customerId, year);

		List<YearlySummaryResponseDTO.MonthlyFeedbackSummaryDTO> months = monthlySummaries.stream()
			.map(YearlySummaryResponseDTO.MonthlyFeedbackSummaryDTO::of)
			.toList();

		return YearlySummaryResponseDTO.of(year, months);
	}

	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId) {

		// 연도/월 검증
		DateValidationUtil.validatePastOrPresentYearMonth(year, month);

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

	@Override
	public MonthlyWeekFeedbackResponseDTO getMonthlyWeekFeedbackResponse(Integer year, Integer month,
		Long customerId, Long trainerId) {

		// 연도/월 검증
		DateValidationUtil.validatePastOrPresentYearMonth(year, month);

		// 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 트레이너 조회
		Trainer trainer = trainerRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		// 해당 트레이너의 고객이 아니라면 접근할 수 없다.
		if (customer.getAssignedTrainer() == null) {
			throw new UserException(UserErrorStatus.TRAINER_NOT_ASSIGNED);
		}

		if (!customer.getAssignedTrainer().getId().equals(trainer.getId())) {
			log.warn("Trainer {} tried to access customer {} who is assigned to trainer {}",
				trainer.getId(), customer.getId(), customer.getAssignedTrainer().getId());
			throw new UserException(UserErrorStatus.NOT_TRAINERS_CUSTOMER);
		}

		// 5. 피드백이 존재하는 주차 목록 조회
		List<Integer> weeks = feedbackRequestRepository
			.findWeeksByCustomerIdAndYearAndMonth(customerId, year, month);

		return MonthlyWeekFeedbackResponseDTO.of(year, month, weeks);

	}

	/**
	 * 완강 전 월별 요약 생성
	 */
	private BeforeCompletedCourseMonthlySummaryDTO buildBeforeCompletionSummary(
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

		// 2. ✅ 모든 주차에 대한 DTO 생성 (빈 주차 포함)
		List<MonthlyWeekFeedbackSummaryResponseDTO> weekDTOs = fillAllWeeksWithData(
			weeklyStats, year, month
		);

		// 3. 월간 집계 계산 (기존과 동일)
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

		Double winningRate = TradingCalculationUtil.calculateWinRate(totalTradingCount, totalWinCount);
		Double monthlyAverageRnr = TradingCalculationUtil.calculateAverageRnR(monthlyPnl, totalRiskTaking);

		MonthlyFeedbackSummaryResponseDTO monthlyFeedback = MonthlyFeedbackSummaryResponseDTO.of(
			weekDTOs,
			winningRate,
			monthlyAverageRnr,
			monthlyPnl
		);

		// 4. 이전 달과 현재 달 성과 비교
		PerformanceComparison<PerformanceComparison.MonthSnapshot> performanceComparison =
			buildMonthlyPerformanceComparison(customerId, year, month, investmentType);

		return BeforeCompletedCourseMonthlySummaryDTO.of(
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
	private AfterCompletedGeneralMonthlySummaryDTO buildAfterCompletionGeneralSummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		List<WeeklyRawData> weeklyStats = feedbackRequestRepository.findWeeklyStatistics(
			customerId, year, month, courseStatus, investmentType
		);

		// ✅ 모든 주차에 대한 DTO 생성 (빈 주차 포함)
		List<MonthlyWeekFeedbackSummaryResponseDTO> weekDTOs = fillAllWeeksWithData(
			weeklyStats, year, month
		);

		// ... 나머지 코드 동일 ...

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

		Double winningRate = TradingCalculationUtil.calculateWinRate(totalTradingCount, totalWinCount);
		Double monthlyAverageRnr = TradingCalculationUtil.calculateAverageRnR(monthlyPnl, totalRiskTaking);

		MonthlyFeedbackSummaryResponseDTO monthlyFeedback = MonthlyFeedbackSummaryResponseDTO.of(
			weekDTOs,
			winningRate,
			monthlyAverageRnr,
			monthlyPnl
		);

		// 진입 타점 통계, 트레이너 평가 등 나머지 코드...
		EntryPointStatistics entryPointStats = feedbackRequestRepository.findEntryPointStatistics(
			customerId, year, month, investmentType
		);

		EntryPointStatisticsResponseDTO entryPointDTO = EntryPointStatisticsResponseDTO.of(
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getReverseCount(),
				entryPointStats.getReverseWinRate(),
				entryPointStats.getReverseRnr()
			),
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getPullBackCount(),
				entryPointStats.getPullBackWinRate(),
				entryPointStats.getPullBackRnr()
			),
			EntryPointStatisticsResponseDTO.PositionDetail.of(
				entryPointStats.getBreakOutCount(),
				entryPointStats.getBreakOutWinRate(),
				entryPointStats.getBreakOutRnr()
			)
		);

		Optional<MonthlyTradingSummary> evaluation = monthlyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_Month(customerId, year, month);

		Boolean isTrainerEvaluated = evaluation.isPresent();
		String monthlyEvaluation = evaluation.map(MonthlyTradingSummary::getMonthlyEvaluation).orElse(null);
		String nextMonthGoal = evaluation.map(MonthlyTradingSummary::getNextMonthGoal).orElse(null);

		PerformanceComparison<PerformanceComparison.MonthSnapshot> performanceComparison =
			buildMonthlyPerformanceComparison(customerId, year, month, investmentType);

		return AfterCompletedGeneralMonthlySummaryDTO.of(
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
	private AfterCompletedScalpingMonthlySummaryDTO buildAfterCompletionScalpingSummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 주차별 통계 조회 (Status 정보 포함)
		List<WeeklyRawData> weeklyStats = feedbackRequestRepository.findWeeklyStatistics(
			customerId, year, month, courseStatus, investmentType
		);

		// 2. 주차별 DTO 변환 (Status 우선순위 기반 결정)
		List<WeeklyFeedbackSummaryDTO> weeklyFeedbacks = weeklyStats.stream()
			.map(stat -> WeeklyFeedbackSummaryDTO.of(
				stat.getWeek(),
				stat.getTradingCount(),
				FeedbackStatusUtil.determineReadStatus(stat.getFnCount())
			))
			.toList();

		return AfterCompletedScalpingMonthlySummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.weeklyFeedbackSummaryDTOS(weeklyFeedbacks)
			.build();
	}

	/**
	 * 월별 성과 비교 생성 (이전 달 vs 현재 달)
	 */
	private PerformanceComparison<PerformanceComparison.MonthSnapshot> buildMonthlyPerformanceComparison(
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

		PerformanceComparison.MonthSnapshot beforeMonth = PerformanceComparison.MonthSnapshot.of(
			previousMonth,
			previousSnapshot.getFinalWinRate(),
			previousSnapshot.getAverageRnr(),
			previousSnapshot.getFinalPnl()
		);

		PerformanceComparison.MonthSnapshot currentMonthSnapshot = PerformanceComparison.MonthSnapshot.of(
			month,
			currentSnapshot.getFinalWinRate(),
			currentSnapshot.getAverageRnr(),
			currentSnapshot.getFinalPnl()
		);

		return PerformanceComparison.of(beforeMonth, currentMonthSnapshot);
	}

	/**
	 * ✅ 모든 주차를 채워서 DTO 리스트 생성
	 * 데이터가 없는 주차는 null 값으로 채웁니다.
	 *
	 * @param weeklyStats DB에서 조회한 주차별 통계
	 * @param year 연도
	 * @param month 월
	 * @return 모든 주차가 포함된 DTO 리스트
	 */
	private List<MonthlyWeekFeedbackSummaryResponseDTO> fillAllWeeksWithData(
		List<WeeklyRawData> weeklyStats,
		Integer year,
		Integer month
	) {
		// 1. 해당 월의 총 주차 수 계산
		int totalWeeks = FeedbackPeriodUtil.getWeeksInMonth(year, month);

		// 2. DB 데이터를 Map으로 변환 (week -> WeeklyRawData)
		Map<Integer, WeeklyRawData> weekDataMap = weeklyStats.stream()
			.collect(Collectors.toMap(
				WeeklyRawData::getWeek,
				stat -> stat
			));

		// 3. 1주차부터 마지막 주차까지 모든 주차 생성
		List<MonthlyWeekFeedbackSummaryResponseDTO> allWeeks = new ArrayList<>();

		for (int week = 1; week <= totalWeeks; week++) {
			WeeklyRawData data = weekDataMap.get(week);

			if (data != null) {
				// ✅ 데이터가 있는 주차
				allWeeks.add(MonthlyWeekFeedbackSummaryResponseDTO.of(
					data.getWeek(),
					data.getTradingCount(),
					data.getWeeklyPnl(),
					FeedbackStatusUtil.determineReadStatus(data.getFnCount())
				));
			} else {
				// ✅ 데이터가 없는 주차 - null 값으로 채움
				allWeeks.add(MonthlyWeekFeedbackSummaryResponseDTO.of(
					week,
					null,  // tradingCount = null
					null,  // weeklyPnl = null
					null   // status = null
				));
			}
		}

		return allWeeks;
	}

}
