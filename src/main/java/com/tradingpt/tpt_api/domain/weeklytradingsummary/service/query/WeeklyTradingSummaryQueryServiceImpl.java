package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
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
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.PerformanceComparison;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DailyRawData;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DirectionStatistics;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.WeeklyPerformanceSnapshot;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.AfterCompletedDayWeeklySummaryDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.AfterCompletedSwingWeeklySummaryDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.BeforeCompletedCourseWeeklySummaryDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DailyFeedbackListItemDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DailyFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DailyFeedbackSummaryDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DirectionStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyDayFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyLossFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyProfitFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyWeekFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyTradingSummaryQueryServiceImpl implements WeeklyTradingSummaryQueryService {

	private final CustomerRepository customerRepository;
	private final TrainerRepository trainerRepository;
	private final UserRepository userRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final WeeklyTradingSummaryRepository weeklyTradingSummaryRepository;

	@Override
	public WeeklySummaryResponseDTO getWeeklyTradingSummary(
		Integer year,
		Integer month,
		Integer week,
		Long customerId
	) {
		// ✅ 연도/월 검증
		DateValidationUtil.validatePastOrPresentYearMonth(year, month);

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 1. 해당 월의 CourseStatus 조회
		CourseStatus courseStatus = feedbackRequestRepository
			.findFirstByFeedbackYearAndFeedbackMonth(customerId, year, month)
			.orElseThrow(() -> new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND))
			.getCourseStatus();

		// 2. 해당 시점의 InvestmentType 조회
		InvestmentType investmentType = investmentTypeHistoryRepository
			.findActiveHistoryForMonth(customerId, year, month)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 3. MembershipLevel 기준 분기 (null은 BASIC과 동일 처리)
		MembershipLevel membershipLevel = customer.getMembershipLevel();

		if (membershipLevel == MembershipLevel.PREMIUM) {
			// PREMIUM: 트레이너 평가 포함
			if (investmentType == InvestmentType.DAY) {
				return buildAfterCompletionDaySummary(
					customerId, year, month, week, courseStatus, investmentType);
			} else {
				return buildAfterCompletionSwingSummary(
					customerId, year, month, week, courseStatus, investmentType);
			}
		} else {
			// BASIC 또는 null: 트레이너 평가 없음
			return buildBeforeCompletionSummary(
				customerId, year, month, week, courseStatus, investmentType);
		}
	}

	@Override
	public WeeklyDayFeedbackResponseDTO getWeeklyDayFeedback(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long userId
	) {
		log.info("Fetching days with feedback for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 사용자 조회 및 역할 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 4. ADMIN이 아닌 경우 (TRAINER인 경우) 담당 고객 검증
		if (user.getRole() != Role.ROLE_ADMIN) {
			Trainer trainer = trainerRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
			validateTrainerAssignment(customer, trainer);
		} else {
			log.info("Admin user (ID: {}) accessing customer {} weekly day feedback", userId, customerId);
		}

		// 5. 피드백이 존재하는 날짜 목록 조회
		List<Integer> days = feedbackRequestRepository
			.findDaysByCustomerIdAndYearAndMonthAndWeek(customerId, year, month, week);

		log.info("Found {} days with feedback for {}-{}-W{}: {}",
			days.size(), year, month, week, days);

		return WeeklyDayFeedbackResponseDTO.of(year, month, week, days);
	}

	@Override
	public DailyFeedbackListResponseDTO getDailyFeedbackList(
		Integer year,
		Integer month,
		Integer week,
		Integer day,
		Long customerId,
		Long userId
	) {
		log.info("Fetching feedbacks for customerId={}, date={}-{}-{} (week {})",
			customerId, year, month, day, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);
		LocalDate date = LocalDate.of(year, month, day);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 사용자 조회 및 역할 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 4. ADMIN이 아닌 경우 (TRAINER인 경우) 담당 고객 검증
		if (user.getRole() != Role.ROLE_ADMIN) {
			Trainer trainer = trainerRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
			validateTrainerAssignment(customer, trainer);
		} else {
			log.info("Admin user (ID: {}) accessing customer {} daily feedback list", userId, customerId);
		}

		// 5. 특정 날짜의 피드백 목록 조회
		List<FeedbackRequest> feedbackRequests = feedbackRequestRepository
			.findByCustomerIdAndDate(customerId, date);

		// 6. DTO 변환
		List<DailyFeedbackListItemDTO> feedbackDTOs = feedbackRequests.stream()
			.map(DailyFeedbackListItemDTO::from)
			.collect(Collectors.toList());

		log.info("Found {} feedbacks for date {}-{}-{}",
			feedbackDTOs.size(), year, month, day);

		return DailyFeedbackListResponseDTO.of(year, month, week, day, feedbackDTOs);
	}

	/**
	 * 트레이너 배정 검증
	 */
	private void validateTrainerAssignment(Customer customer, Trainer trainer) {
		if (customer.getAssignedTrainer() == null) {
			throw new UserException(UserErrorStatus.TRAINER_NOT_ASSIGNED);
		}

		if (!customer.getAssignedTrainer().getId().equals(trainer.getId())) {
			log.warn("Trainer {} tried to access customer {} who is assigned to user {}",
				trainer.getId(), customer.getId(), customer.getAssignedTrainer().getId());
			throw new UserException(UserErrorStatus.NOT_TRAINERS_CUSTOMER);
		}
	}

	/**
	 * 완강 전 주간 요약 생성
	 */
	private BeforeCompletedCourseWeeklySummaryDTO buildBeforeCompletionSummary(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 일별 통계 조회
		List<DailyRawData> dailyStats = feedbackRequestRepository.findDailyStatistics(
			customerId, year, month, week, courseStatus, investmentType
		);

		// 2. 모든 요일을 포함한 일별 DTO 생성
		List<WeeklyWeekFeedbackSummaryResponseDTO> dailyDTOs = fillAllWeekdays(
			dailyStats, year, month, week
		);

		// 3. 주간 집계 계산
		Integer totalTradingCount = dailyStats.stream()
			.mapToInt(DailyRawData::getTradingCount)
			.sum();

		Integer totalWinCount = dailyStats.stream()
			.mapToInt(DailyRawData::getWinCount)
			.sum();

		BigDecimal weeklyPnl = dailyStats.stream()
			.map(DailyRawData::getDailyPnl)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		// ✅ 수익 매매들의 평균 R&R 계산 (pnl > 0인 매매의 rnr 컬럼 평균)
		Double weeklyAverageRnr = feedbackRequestRepository.findAverageRnRForWeeklySummary(
			customerId, year, month, week, courseStatus, investmentType
		);

		Double winningRate = TradingCalculationUtil.calculateWinRate(
			totalTradingCount, totalWinCount);

		WeeklyFeedbackSummaryResponseDTO weeklyFeedback = WeeklyFeedbackSummaryResponseDTO.builder()
			.weeklyWeekFeedbackSummaryResponseDTOS(dailyDTOs)
			.winningRate(winningRate)
			.weeklyAverageRnr(weeklyAverageRnr)
			.weeklyPnl(weeklyPnl)
			.build();

		// 4. 이전 주와 현재 주 성과 비교
		PerformanceComparison<PerformanceComparison.WeekSnapshot> performanceComparison =
			buildWeeklyPerformanceComparison(customerId, year, month, week, investmentType);

		// 5. 메모 조회
		Optional<WeeklyTradingSummary> summary = weeklyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
				customerId, year, month, week);
		String memo = summary.map(WeeklyTradingSummary::getMemo).orElse(null);

		return BeforeCompletedCourseWeeklySummaryDTO.of(
			courseStatus, investmentType, year, month, week, weeklyFeedback, performanceComparison, memo
		);
	}

	/**
	 * 완강 후 데이 트레이딩 주간 요약 생성
	 */
	private AfterCompletedDayWeeklySummaryDTO buildAfterCompletionDaySummary(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 일별 통계 조회
		List<DailyRawData> dailyStats = feedbackRequestRepository.findDailyStatistics(
			customerId, year, month, week, courseStatus, investmentType
		);

		// 2. 모든 요일을 포함한 일별 DTO 생성
		List<WeeklyWeekFeedbackSummaryResponseDTO> dailyDTOs = fillAllWeekdays(
			dailyStats, year, month, week
		);

		// 3. 주간 집계 계산
		Integer totalTradingCount = dailyStats.stream()
			.mapToInt(DailyRawData::getTradingCount)
			.sum();

		Integer totalWinCount = dailyStats.stream()
			.mapToInt(DailyRawData::getWinCount)
			.sum();

		BigDecimal weeklyPnl = dailyStats.stream()
			.map(DailyRawData::getDailyPnl)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		// ✅ 수익 매매들의 평균 R&R 계산 (pnl > 0인 매매의 rnr 컬럼 평균)
		Double weeklyAverageRnr = feedbackRequestRepository.findAverageRnRForWeeklySummary(
			customerId, year, month, week, courseStatus, investmentType
		);

		Double winningRate = TradingCalculationUtil.calculateWinRate(
			totalTradingCount, totalWinCount);

		WeeklyFeedbackSummaryResponseDTO weeklyFeedback = WeeklyFeedbackSummaryResponseDTO.builder()
			.weeklyWeekFeedbackSummaryResponseDTOS(dailyDTOs)
			.winningRate(winningRate)
			.weeklyAverageRnr(weeklyAverageRnr)
			.weeklyPnl(weeklyPnl)
			.build();

		// 4. 이전 주와 현재 주 성과 비교
		PerformanceComparison<PerformanceComparison.WeekSnapshot> performanceComparison =
			buildWeeklyPerformanceComparison(customerId, year, month, week, investmentType);

		// 5. 방향성 통계 조회
		DirectionStatistics directionStats = feedbackRequestRepository.findDirectionStatistics(
			customerId, year, month, week
		);

		DirectionStatisticsResponseDTO directionDTO = DirectionStatisticsResponseDTO.builder()
			.o(DirectionStatisticsResponseDTO.DirectionDetail.of(
				directionStats.getDirectionOCount(),
				directionStats.getDirectionOWinRate(),
				directionStats.getDirectionORnr()
			))
			.x(DirectionStatisticsResponseDTO.DirectionDetail.of(
				directionStats.getDirectionXCount(),
				directionStats.getDirectionXWinRate(),
				directionStats.getDirectionXRnr()
			))
			.build();

		// 6. 트레이너 평가 조회
		Optional<WeeklyTradingSummary> evaluation = weeklyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
				customerId, year, month, week);

		String weeklyLossTradingAnalysis = evaluation
			.map(WeeklyTradingSummary::getWeeklyLossTradingAnalysis).orElse(null);
		String weeklyProfitableTradingAnalysis = evaluation
			.map(WeeklyTradingSummary::getWeeklyProfitableTradingAnalysis).orElse(null);
		String weeklyEvaluation = evaluation
			.map(WeeklyTradingSummary::getWeeklyEvaluation).orElse(null);

		return AfterCompletedDayWeeklySummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.week(week)
			.weeklyFeedbackSummaryResponseDTO(weeklyFeedback)
			.performanceComparison(performanceComparison)
			.directionStatisticsResponseDTO(directionDTO)
			.weeklyLossTradingAnalysis(weeklyLossTradingAnalysis)
			.weeklyProfitableTradingAnalysis(weeklyProfitableTradingAnalysis)
			.weeklyEvaluation(weeklyEvaluation)
			.build();
	}

	/**
	 * 완강 후 스윙 주간 요약 생성
	 */
	private AfterCompletedSwingWeeklySummaryDTO buildAfterCompletionSwingSummary(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		// 1. 일별 통계 조회
		List<DailyRawData> dailyStats = feedbackRequestRepository.findDailyStatistics(
			customerId, year, month, week, courseStatus, investmentType
		);

		// 2. 일별 DTO 변환
		List<DailyFeedbackSummaryDTO> dailyFeedbacks = dailyStats.stream()
			.map(stat -> DailyFeedbackSummaryDTO.of(
				stat.getDate(), stat.getTradingCount(), FeedbackStatusUtil.determineReadStatus(stat.getFnCount())
			))
			.toList();

		return AfterCompletedSwingWeeklySummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.week(week)
			.dailyFeedbackSummaryDTOS(dailyFeedbacks)
			.build();
	}

	// ========================================
	// Private Helper Methods
	// ========================================

	/**
	 * 해당 주차의 모든 날짜를 채워서 DTO 리스트 생성
	 * 빈 날짜는 null 값으로 채움
	 *
	 * @param dailyStats DB에서 조회한 일별 통계
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 해당 주차의 모든 날짜가 포함된 DTO 리스트
	 */
	private List<WeeklyWeekFeedbackSummaryResponseDTO> fillAllWeekdays(
		List<DailyRawData> dailyStats,
		Integer year,
		Integer month,
		Integer week
	) {
		// 해당 주차의 날짜 범위 가져오기
		FeedbackPeriodUtil.WeekDateRange weekRange =
			FeedbackPeriodUtil.getWeekDateRange(year, month, week);

		LocalDate startOfWeek = weekRange.startDate();
		LocalDate endOfWeek = weekRange.endDate();

		// DB 데이터를 Map으로 변환
		Map<LocalDate, DailyRawData> dataMap = dailyStats.stream()
			.collect(Collectors.toMap(
				DailyRawData::getDate,
				stat -> stat
			));

		// 해당 주차의 모든 날짜 생성
		List<WeeklyWeekFeedbackSummaryResponseDTO> allDays = new ArrayList<>();
		LocalDate current = startOfWeek;

		while (!current.isAfter(endOfWeek)) {
			DailyRawData data = dataMap.get(current);

			if (data != null) {
				// 데이터가 있는 날짜
				Integer winCount = data.getWinCount();
				Integer lossCount = data.getTradingCount() - winCount;

				allDays.add(WeeklyWeekFeedbackSummaryResponseDTO.builder()
					.date(data.getDate())
					.tradingCount(data.getTradingCount())
					.winCount(winCount)
					.lossCount(lossCount)
					.dailyPnl(data.getDailyPnl().doubleValue())
					.status(FeedbackStatusUtil.determineReadStatus(data.getFnCount()))
					.build());
			} else {
				// 데이터가 없는 날짜 - null 값으로 채움
				allDays.add(WeeklyWeekFeedbackSummaryResponseDTO.builder()
					.date(current)
					.tradingCount(null)
					.winCount(null)
					.lossCount(null)
					.dailyPnl(null)
					.status(null)
					.build());
			}

			current = current.plusDays(1);
		}

		return allDays;
	}

	/**
	 * 주간 성과 비교 생성 (이전 주 vs 현재 주)
	 */
	private PerformanceComparison<PerformanceComparison.WeekSnapshot> buildWeeklyPerformanceComparison(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	) {
		// 현재 주 성과
		WeeklyPerformanceSnapshot currentSnapshot = feedbackRequestRepository.findWeeklyPerformance(
			customerId, year, month, week, investmentType
		);

		// 이전 주 계산
		Integer previousWeek = week - 1;
		Integer previousMonth = month;
		Integer previousYear = year;

		if (previousWeek < 1) {
			// 이전 달의 마지막 주
			LocalDate previousMonthDate = LocalDate.of(year, month, 1).minusMonths(1);
			previousYear = previousMonthDate.getYear();
			previousMonth = previousMonthDate.getMonthValue();
			previousWeek = FeedbackPeriodUtil.getWeeksInMonth(previousYear, previousMonth);
		}

		// 이전 주 성과
		WeeklyPerformanceSnapshot previousSnapshot = feedbackRequestRepository.findWeeklyPerformance(
			customerId, previousYear, previousMonth, previousWeek, investmentType
		);

		PerformanceComparison.WeekSnapshot beforeWeek = PerformanceComparison.WeekSnapshot.of(
			previousWeek,
			previousSnapshot.getFinalWinRate(),
			previousSnapshot.getAverageRnr(),
			previousSnapshot.getFinalPnl()
		);

		PerformanceComparison.WeekSnapshot currentWeekSnapshot = PerformanceComparison.WeekSnapshot.of(
			week,
			currentSnapshot.getFinalWinRate(),
			currentSnapshot.getAverageRnr(),
			currentSnapshot.getFinalPnl()
		);

		return PerformanceComparison.of(beforeWeek, currentWeekSnapshot);
	}

	@Override
	public WeeklyProfitFeedbackListResponseDTO getProfitFeedbacksByWeek(
		Integer year,
		Integer month,
		Integer week,
		Long customerId
	) {
		log.info("Fetching profit feedbacks for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 이익 매매 피드백 목록 조회 (완강 전, pnl > 0)
		List<FeedbackRequest> profitFeedbacks = feedbackRequestRepository
			.findProfitFeedbacksByCustomerAndYearAndMonthAndWeek(customerId, year, month, week);

		log.info("Found {} profit feedbacks for {}-{}-W{}",
			profitFeedbacks.size(), year, month, week);

		return WeeklyProfitFeedbackListResponseDTO.of(year, month, week, profitFeedbacks);
	}

	@Override
	public WeeklyLossFeedbackListResponseDTO getLossFeedbacksByWeek(
		Integer year,
		Integer month,
		Integer week,
		Long customerId
	) {
		log.info("Fetching loss feedbacks for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 손실 매매 피드백 목록 조회 (완강 전, pnl <= 0)
		List<FeedbackRequest> lossFeedbacks = feedbackRequestRepository
			.findLossFeedbacksByCustomerAndYearAndMonthAndWeek(customerId, year, month, week);

		log.info("Found {} loss feedbacks for {}-{}-W{}",
			lossFeedbacks.size(), year, month, week);

		return WeeklyLossFeedbackListResponseDTO.of(year, month, week, lossFeedbacks);
	}
}
