package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.UpsertMonthlyEvaluationRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryErrorStatus;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryException;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
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
	private final UserRepository userRepository;

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

		User trainer = customer.getAssignedTrainer();
		if (trainer == null) {
			log.error("Customer {} has no assigned user", customerId);
			throw new UserException(UserErrorStatus.TRAINER_NOT_FOUND);
		}

		// 2. 해당 연/월의 투자 타입 조회
		InvestmentType investmentType = investmentTypeHistoryRepository
			.findActiveHistoryForMonth(customerId, year, month)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 3. 중복 체크
		boolean alreadyExists = monthlyTradingSummaryRepository
			.existsByCustomerIdAndYearAndMonthAndInvestmentType(
				customerId, year, month, investmentType);

		if (alreadyExists) {
			log.warn("Monthly summary already exists for customerId={}, year={}, month={}, investmentType={}",
				customerId, year, month, investmentType);
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.MONTHLY_SUMMARY_ALREADY_EXISTS);
		}

		// 5. 멤버십 레벨 검증 (PREMIUM만 평가 대상)
		validateMembershipLevel(customer);

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
	 * 고객의 멤버십 레벨이 PREMIUM인지 검증합니다.
	 * PREMIUM 고객만 트레이너 평가 작성 대상입니다.
	 * (완강 여부와 무관하게 PREMIUM이면 평가 작성 가능)
	 *
	 * @param customer 고객 엔티티
	 * @throws MonthlyTradingSummaryException PREMIUM이 아닌 경우
	 */
	private void validateMembershipLevel(Customer customer) {
		MembershipLevel membershipLevel = customer.getMembershipLevel();

		// PREMIUM이 아닌 경우 (null 또는 BASIC) 평가 작성 불가
		if (membershipLevel != MembershipLevel.PREMIUM) {
			log.warn("Customer is not PREMIUM: customerId={}, membershipLevel={}",
				customer.getId(), membershipLevel);
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.MEMBERSHIP_NOT_PREMIUM);
		}

		log.debug("Membership level validated for customerId={}, membershipLevel={}",
			customer.getId(), membershipLevel);
	}

	/**
	 * =============================
	 * UPSERT METHODS (DDD 패턴)
	 * =============================
	 */

	/**
	 * 월간 매매일지 평가 Upsert (ADMIN/TRAINER용 - 완강 후)
	 * Entity의 비즈니스 메서드를 통해 검증 및 상태 변경
	 * JPA Dirty Checking을 활용하여 자동 UPDATE
	 */
	@Override
	public MonthlyEvaluationResponseDTO upsertMonthlyEvaluationByTrainer(
		Integer year,
		Integer month,
		Long customerId,
		Long evaluatorId,
		UpsertMonthlyEvaluationRequestDTO request
	) {
		log.info("Upserting monthly evaluation for customerId={}, year={}, month={}, evaluatorId={}",
			customerId, year, month, evaluatorId);

		// 1. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 2. 평가 작성자 조회 (ADMIN 또는 TRAINER)
		User evaluator = userRepository.findById(evaluatorId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 3. 권한 검증 (ADMIN 또는 TRAINER만 작성 가능)
		if (evaluator.getRole() != Role.ROLE_ADMIN && evaluator.getRole() != Role.ROLE_TRAINER) {
			log.warn("User is not ADMIN or TRAINER: evaluatorId={}, role={}", evaluatorId, evaluator.getRole());
			throw new UserException(UserErrorStatus.USER_NOT_FOUND);
		}

		// 4. 해당 연/월의 투자 타입 조회
		InvestmentType investmentType = investmentTypeHistoryRepository
			.findActiveHistoryForMonth(customerId, year, month)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();

		// 5. 콘텐츠 처리
		String processedEvaluation = contentImageUploader.processContent(
			request.getMonthlyEvaluation(),
			"monthly-summaries"
		);
		String processedGoal = contentImageUploader.processContent(
			request.getNextMonthGoal(),
			"monthly-summaries"
		);

		// 6. 기존 요약 조회 (Upsert 패턴)
		MonthlyTradingSummary summary = monthlyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_Month(customerId, year, month)
			.orElse(null);

		if (summary != null) {
			// UPDATE: Entity의 DDD 비즈니스 메서드 호출 (내부에서 검증)
			summary.updateTrainerEvaluation(processedEvaluation, processedGoal);
			log.info("Updated monthly evaluation by evaluatorId={}", evaluatorId);
		} else {
			// CREATE: 멤버십 레벨 검증 후 새 Entity 생성 (PREMIUM만)
			validateMembershipLevel(customer);

			summary = MonthlyTradingSummary.createForEvaluation(
				processedEvaluation,
				processedGoal,
				customer,
				evaluator,
				investmentType,
				year,
				month
			);
			monthlyTradingSummaryRepository.save(summary);
			log.info("Created monthly evaluation by evaluatorId={}", evaluatorId);
		}

		// JPA Dirty Checking으로 자동 UPDATE (save() 불필요)
		return MonthlyEvaluationResponseDTO.from(summary);
	}
}