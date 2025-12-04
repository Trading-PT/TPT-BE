package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.DateValidationUtil;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.UpsertWeeklyEvaluationRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.UpsertWeeklyMemoRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyMemoResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.exception.WeeklyTradingSummaryErrorStatus;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.exception.WeeklyTradingSummaryException;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;
import com.tradingpt.tpt_api.global.infrastructure.content.ContentImageUploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyTradingSummaryCommandServiceImpl implements WeeklyTradingSummaryCommandService {

	private final CustomerRepository customerRepository;
	private final WeeklyTradingSummaryRepository weeklyTradingSummaryRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final ContentImageUploader contentImageUploader;
	private final TrainerRepository trainerRepository;

	/**
	 * =============================
	 * TRAINER가 작성
	 * =============================
	 */
	@Override
	public Void createWeeklyTradingSummaryByTrainer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long trainerId,
		CreateWeeklyTradingSummaryRequestDTO request
	) {
		log.info("Trainer creating weekly summary for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 트레이너 조회
		Trainer trainer = trainerRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		// 4. 해당 주의 투자 타입 조회
		InvestmentType investmentType = getInvestmentTypeForWeek(customerId, year, month, week);

		// 5. 해당 주의 코스 상태 조회
		CourseStatus courseStatus = getCourseStatusForWeek(customerId, year, month, week);

		// 6. ✅ 완강 전인 경우 트레이너는 작성 불가
		if (courseStatus == CourseStatus.BEFORE_COMPLETION) {
			log.warn("Trainer cannot create summary for BEFORE_COMPLETION: trainerId={}", trainerId);
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.TRAINER_CANNOT_CREATE_FOR_BEFORE_COMPLETION);
		}

		// 7. ✅ 완강 후 + SWING인 경우 생성 불가
		if (courseStatus == CourseStatus.AFTER_COMPLETION && investmentType != InvestmentType.DAY) {
			log.warn("Trainer cannot create summary for AFTER_COMPLETION + non-DAY: investmentType={}",
				investmentType);
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.TRAINER_CANNOT_CREATE_FOR_NON_DAY_AFTER_COMPLETION);
		}

		// 8. 중복 체크
		checkDuplicateSummary(customerId, year, month, week, investmentType);

		// 9. ✅ 입력 데이터 검증 (트레이너용)
		validateTrainerRequestData(request);

		// 10. ✅ 콘텐츠 처리 및 저장 (상세 평가만)
		String processedEvaluation = contentImageUploader.processContent(
			request.getWeeklyEvaluation(),
			"weekly-summaries"
		);

		String processedProfitAnalysis = contentImageUploader.processContent(
			request.getWeeklyProfitableTradingAnalysis(),
			"weekly-summaries"
		);

		String processedLossAnalysis = contentImageUploader.processContent(
			request.getWeeklyLossTradingAnalysis(),
			"weekly-summaries"
		);

		WeeklyTradingSummary summary = WeeklyTradingSummary.createFromProcessed(
			null,  // memo는 null
			processedEvaluation,
			processedProfitAnalysis,
			processedLossAnalysis,
			customer,
			trainer,
			courseStatus,
			investmentType,
			year,
			month,
			week
		);

		weeklyTradingSummaryRepository.save(summary);

		log.info("Trainer created weekly summary successfully");
		return null;
	}

	/**
	 * =============================
	 * CUSTOMER가 작성
	 * =============================
	 */
	@Override
	public Void createWeeklyTradingSummaryByCustomer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		CreateWeeklyTradingSummaryRequestDTO request
	) {
		log.info("Customer creating weekly summary for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 해당 주의 투자 타입 조회
		InvestmentType investmentType = getInvestmentTypeForWeek(customerId, year, month, week);

		// 4. 해당 주의 코스 상태 조회
		CourseStatus courseStatus = getCourseStatusForWeek(customerId, year, month, week);

		// 5. ✅ 완강 후인 경우 고객은 작성 불가
		if (courseStatus == CourseStatus.AFTER_COMPLETION) {
			log.warn("Customer cannot create summary for AFTER_COMPLETION: customerId={}", customerId);
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.CUSTOMER_CANNOT_CREATE_FOR_AFTER_COMPLETION);
		}

		// 6. 중복 체크
		checkDuplicateSummary(customerId, year, month, week, investmentType);

		// 7. ✅ 입력 데이터 검증 (고객용)
		validateCustomerRequestData(request);

		// 8. ✅ 콘텐츠 처리 및 저장 (memo만)
		String processedMemo = contentImageUploader.processContent(
			request.getMemo(),
			"weekly-summaries"
		);

		// 9. 트레이너 조회 (멤버십 미가입 고객은 트레이너가 없을 수 있음)
		Trainer trainer = null;
		if (customer.getAssignedTrainer() != null) {
			trainer = trainerRepository.findById(customer.getAssignedTrainer().getId())
				.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
		}

		WeeklyTradingSummary summary = WeeklyTradingSummary.createFromProcessed(
			processedMemo,
			null,  // 상세 평가는 null
			null,
			null,
			customer,
			trainer,
			courseStatus,
			investmentType,
			year,
			month,
			week
		);

		weeklyTradingSummaryRepository.save(summary);

		log.info("Customer created weekly summary successfully");
		return null;
	}

	/**
	 * =============================
	 * PRIVATE HELPER METHODS
	 * =============================
	 */

	/**
	 * 해당 주의 투자 타입 조회
	 */
	private InvestmentType getInvestmentTypeForWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		return investmentTypeHistoryRepository.findActiveHistoryForMonth(
				customerId, year, month)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND))
			.getInvestmentType();
	}

	/**
	 * 해당 주의 코스 상태 조회
	 */
	private CourseStatus getCourseStatusForWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		return feedbackRequestRepository
			.findFirstByCustomer_IdAndFeedbackYearAndFeedbackMonthAndFeedbackWeekOrderByCreatedAtAsc(
				customerId, year, month, week)
			.map(FeedbackRequest::getCourseStatus)
			.orElse(CourseStatus.BEFORE_COMPLETION);
	}

	/**
	 * 중복 체크
	 */
	private void checkDuplicateSummary(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	) {
		boolean alreadyExists = weeklyTradingSummaryRepository
			.existsByCustomerIdAndYearAndMonthAndWeekAndInvestmentType(
				customerId, year, month, week, investmentType);

		if (alreadyExists) {
			log.warn("Weekly summary already exists");
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.WEEKLY_SUMMARY_ALREADY_EXISTS);
		}
	}

	/**
	 * ✅ 트레이너 요청 데이터 검증
	 * - memo 있으면 에러
	 * - 상세 평가 3개 모두 필수
	 */
	private void validateTrainerRequestData(CreateWeeklyTradingSummaryRequestDTO request) {
		boolean hasMemo = StringUtils.hasText(request.getMemo());
		boolean hasEvaluation = StringUtils.hasText(request.getWeeklyEvaluation());
		boolean hasProfitAnalysis = StringUtils.hasText(request.getWeeklyProfitableTradingAnalysis());
		boolean hasLossAnalysis = StringUtils.hasText(request.getWeeklyLossTradingAnalysis());

		// memo가 있으면 에러
		if (hasMemo) {
			log.warn("Trainer cannot write memo for AFTER_COMPLETION");
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.MEMO_NOT_ALLOWED_FOR_TRAINER_AFTER_COMPLETION);
		}

		// 상세 평가 3개가 모두 있어야 함
		if (!hasEvaluation || !hasProfitAnalysis || !hasLossAnalysis) {
			log.warn("All detailed evaluations are required for user");
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.DETAILED_EVALUATION_INCOMPLETE);
		}
	}

	/**
	 * ✅ 고객 요청 데이터 검증
	 * - memo 필수
	 * - 상세 평가 있으면 에러
	 */
	private void validateCustomerRequestData(CreateWeeklyTradingSummaryRequestDTO request) {
		boolean hasMemo = StringUtils.hasText(request.getMemo());
		boolean hasEvaluation = StringUtils.hasText(request.getWeeklyEvaluation());
		boolean hasProfitAnalysis = StringUtils.hasText(request.getWeeklyProfitableTradingAnalysis());
		boolean hasLossAnalysis = StringUtils.hasText(request.getWeeklyLossTradingAnalysis());

		boolean hasAnyDetailedEvaluation = hasEvaluation || hasProfitAnalysis || hasLossAnalysis;

		// memo가 없으면 에러
		if (!hasMemo) {
			log.warn("Memo is required for customer");
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.MEMO_REQUIRED_FOR_CUSTOMER_BEFORE_COMPLETION);
		}

		// 상세 평가가 있으면 에러
		if (hasAnyDetailedEvaluation) {
			log.warn("Customer cannot write detailed evaluation");
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.DETAILED_EVALUATION_NOT_ALLOWED_FOR_CUSTOMER);
		}
	}

	/**
	 * =============================
	 * UPSERT METHODS (DDD 패턴)
	 * =============================
	 */

	/**
	 * 주간 매매일지 메모 Upsert (고객용 - 완강 전)
	 * Entity의 비즈니스 메서드를 통해 검증 및 상태 변경
	 * JPA Dirty Checking을 활용하여 자동 UPDATE
	 */
	@Override
	public WeeklyMemoResponseDTO upsertWeeklyMemoByCustomer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		UpsertWeeklyMemoRequestDTO request
	) {
		log.info("Customer upserting weekly memo for customerId={}, year={}, month={}, week={}",
			customerId, year, month, week);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 해당 주의 투자 타입 조회
		InvestmentType investmentType = getInvestmentTypeForWeek(customerId, year, month, week);

		// 4. 콘텐츠 처리
		String processedMemo = contentImageUploader.processContent(
			request.getMemo(),
			"weekly-summaries"
		);

		// 5. 기존 요약 조회 (Upsert 패턴)
		WeeklyTradingSummary summary = weeklyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
				customerId, year, month, week)
			.orElse(null);

		if (summary != null) {
			// UPDATE: Entity의 DDD 비즈니스 메서드 호출 (내부에서 검증)
			summary.updateCustomerMemo(processedMemo);
			log.info("Customer updated weekly memo");
		} else {
			// CREATE: 트레이너 조회 후 새 Entity 생성
			Trainer trainer = null;
			if (customer.getAssignedTrainer() != null) {
				trainer = trainerRepository.findById(customer.getAssignedTrainer().getId())
					.orElse(null);
			}

			summary = WeeklyTradingSummary.createForCustomerMemo(
				processedMemo,
				customer,
				trainer,
				investmentType,
				year,
				month,
				week
			);
			weeklyTradingSummaryRepository.save(summary);
			log.info("Customer created weekly memo");
		}

		// JPA Dirty Checking으로 자동 UPDATE (save() 불필요)
		return WeeklyMemoResponseDTO.from(summary);
	}

	/**
	 * 주간 매매일지 트레이너 평가 Upsert (트레이너/관리자용 - 완강 후 + DAY 타입)
	 * Entity의 비즈니스 메서드를 통해 검증 및 상태 변경
	 * JPA Dirty Checking을 활용하여 자동 UPDATE
	 */
	@Override
	public WeeklyEvaluationResponseDTO upsertWeeklyEvaluationByTrainer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long trainerId,
		UpsertWeeklyEvaluationRequestDTO request
	) {
		log.info("Trainer upserting weekly evaluation for customerId={}, year={}, month={}, week={}, trainerId={}",
			customerId, year, month, week, trainerId);

		// 1. 날짜 검증
		DateValidationUtil.validateYearMonthWeek(year, month, week);

		// 2. 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3. 트레이너 조회
		Trainer trainer = trainerRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		// 4. 해당 주의 투자 타입 조회
		InvestmentType investmentType = getInvestmentTypeForWeek(customerId, year, month, week);

		// 5. 콘텐츠 처리
		String processedEvaluation = contentImageUploader.processContent(
			request.getWeeklyEvaluation(),
			"weekly-summaries"
		);
		String processedProfitAnalysis = contentImageUploader.processContent(
			request.getWeeklyProfitableTradingAnalysis(),
			"weekly-summaries"
		);
		String processedLossAnalysis = contentImageUploader.processContent(
			request.getWeeklyLossTradingAnalysis(),
			"weekly-summaries"
		);

		// 6. 기존 요약 조회 (Upsert 패턴)
		WeeklyTradingSummary summary = weeklyTradingSummaryRepository
			.findByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
				customerId, year, month, week)
			.orElse(null);

		if (summary != null) {
			// UPDATE: Entity의 DDD 비즈니스 메서드 호출 (내부에서 검증)
			summary.updateTrainerEvaluation(
				processedEvaluation,
				processedProfitAnalysis,
				processedLossAnalysis
			);
			log.info("Trainer updated weekly evaluation");
		} else {
			// CREATE: 새 Entity 생성
			summary = WeeklyTradingSummary.createForTrainerEvaluation(
				processedEvaluation,
				processedProfitAnalysis,
				processedLossAnalysis,
				customer,
				trainer,
				investmentType,
				year,
				month,
				week
			);
			weeklyTradingSummaryRepository.save(summary);
			log.info("Trainer created weekly evaluation");
		}

		// JPA Dirty Checking으로 자동 UPDATE (save() 불필요)
		return WeeklyEvaluationResponseDTO.from(summary);
	}
}