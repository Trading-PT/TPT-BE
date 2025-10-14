package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryErrorStatus;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryException;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.content.ContentImageUploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyTradingSummaryCommandServiceImpl implements MonthlyTradingSummaryCommandService {

	private final CustomerRepository customerRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final ContentImageUploader contentImageUploader;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;

	@Override
	public Void createMonthlySummary(
		Integer year,
		Integer month,
		Long customerId,
		CreateMonthlyTradingSummaryRequestDTO request
	) {
		log.info("Creating monthly summary for customerId={}, year={}, month={}",
			customerId, year, month);

		// 1. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		Trainer trainer = customer.getAssignedTrainer();
		if (trainer == null) {
			log.error("Customer {} has no assigned trainer", customerId);
			throw new UserException(UserErrorStatus.TRAINER_NOT_FOUND);
		}

		// 2. 해당 연/월의 투자 타입 조회
		InvestmentType investmentType = investmentTypeHistoryRepository
			.findActiveHistoryForMonth(customerId, year, month)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 3. ✅ 투자 타입 검증 (DAY 또는 SWING만 허용)
		validateInvestmentType(investmentType);

		// 4. 중복 체크
		boolean alreadyExists = monthlyTradingSummaryRepository
			.existsByCustomerIdAndYearAndMonthAndInvestmentType(
				customerId, year, month, investmentType);

		if (alreadyExists) {
			log.warn("Monthly summary already exists for customerId={}, year={}, month={}, investmentType={}",
				customerId, year, month, investmentType);
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.MONTHLY_SUMMARY_ALREADY_EXISTS);
		}

		// 5. 완강 여부 검증
		validateCourseCompletion(customerId, year, month);

		// 6. 콘텐츠 처리 (저장 전에 처리 완료)
		String processedEvaluation = contentImageUploader.processContent(
			request.getMonthlyEvaluation(),
			"monthly-summaries"
		);

		String processedGoal = contentImageUploader.processContent(
			request.getNextMonthGoal(),
			"monthly-summaries"
		);

		// 7. ✅ 한 번에 저장 (investmentType 전달)
		MonthlyTradingSummary summary = MonthlyTradingSummary.createFromProcessed(
			processedEvaluation,
			processedGoal,
			customer,
			trainer,
			year,
			month,
			investmentType
		);

		monthlyTradingSummaryRepository.save(summary);

		log.info("Monthly summary created successfully for customerId={}, year={}, month={}, investmentType={}",
			customerId, year, month, investmentType);

		return null;
	}

	/**
	 * 투자 타입 검증
	 * 월간 요약은 DAY 또는 SWING 타입에서만 작성 가능
	 *
	 * @param investmentType 투자 타입
	 * @throws MonthlyTradingSummaryException SCALPING 타입인 경우
	 */
	private void validateInvestmentType(InvestmentType investmentType) {
		if (investmentType == InvestmentType.SCALPING) {
			log.warn("Attempted to create monthly summary for SCALPING type");
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.INVALID_INVESTMENT_TYPE);
		}

		log.debug("Investment type validation passed: {}", investmentType);
	}

	/**
	 * 해당 연/월에 완강 후(AFTER_COMPLETION) 상태의 피드백이 존재하는지 검증합니다.
	 *
	 * @param customerId 고객 ID
	 * @param year 연도
	 * @param month 월
	 * @throws MonthlyTradingSummaryException 완강 후 피드백이 없는 경우
	 */
	private void validateCourseCompletion(Long customerId, Integer year, Integer month) {
		boolean hasCompletedCourse = feedbackRequestRepository
			.existsByCustomerIdAndYearAndMonthAndCourseStatus(
				customerId, year, month, CourseStatus.AFTER_COMPLETION);

		if (!hasCompletedCourse) {
			log.warn("No AFTER_COMPLETION feedback found for customerId={}, year={}, month={}",
				customerId, year, month);
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.COURSE_NOT_COMPLETED);
		}

		log.debug("Course completion validated for customerId={}, year={}, month={}",
			customerId, year, month);
	}
}