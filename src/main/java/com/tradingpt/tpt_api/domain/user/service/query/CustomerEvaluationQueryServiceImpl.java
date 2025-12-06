package com.tradingpt.tpt_api.domain.user.service.query;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.YearMonthProjection;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationListResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 평가 관리 Query Service 구현체
 *
 * 역할별 동작:
 * - ADMIN: 모든 고객의 미작성 평가 목록 조회
 * - TRAINER: 담당 고객의 미작성 평가 목록만 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerEvaluationQueryServiceImpl implements CustomerEvaluationQueryService {

	private final CustomerRepository customerRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;
	private final WeeklyTradingSummaryRepository weeklytradingSummaryRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final TrainerRepository trainerRepository;
	private final UserRepository userRepository;

	@Override
	public PendingEvaluationListResponseDTO getPendingEvaluations(Long userId, Pageable pageable) {
		// 1. 사용자 조회 및 역할 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 2. Role에 따라 다른 조회 로직 실행 (MembershipLevel 기준)
		Slice<Customer> customerSlice;
		if (user.getRole() == Role.ROLE_ADMIN) {
			// ADMIN: 모든 PREMIUM 고객 조회
			customerSlice = customerRepository.findByMembershipLevelOrderByNameAsc(
				MembershipLevel.PREMIUM,
				pageable
			);
			log.info("Admin user (ID: {}) fetching all PREMIUM customers' pending evaluations", userId);
		} else {
			// TRAINER: 담당 PREMIUM 고객만 조회
			customerSlice = customerRepository.findByAssignedTrainerIdAndMembershipLevelOrderByNameAsc(
				userId,
				MembershipLevel.PREMIUM,
				pageable
			);
			log.info("Trainer (ID: {}) fetching assigned PREMIUM customers' pending evaluations", userId);
		}

		// 3. 조회된 고객들의 미작성 평가 목록 생성
		List<PendingEvaluationItemDTO> allPendingEvaluations = new ArrayList<>();

		for (Customer customer : customerSlice.getContent()) {
			// 고객별 미작성 평가 목록 생성 및 추가 (FeedbackRequest 기반)
			List<PendingEvaluationItemDTO> customerPendingEvaluations =
				generatePendingEvaluationsForCustomer(customer);

			allPendingEvaluations.addAll(customerPendingEvaluations);
		}

		// 4. Slice 정보 생성 (고객 Slice 기반)
		Slice<PendingEvaluationItemDTO> evaluationSlice = new SliceImpl<>(
			allPendingEvaluations,
			pageable,
			customerSlice.hasNext()
		);

		// 5. Response DTO 변환
		return PendingEvaluationListResponseDTO.of(evaluationSlice);
	}

	/**
	 * 특정 고객의 FeedbackRequest가 존재하는 월에 대한 미작성 평가 목록 생성
	 * (기존 completedAt 기반에서 FeedbackRequest 존재 기반으로 변경)
	 *
	 * @param customer 고객 엔티티
	 * @return 미작성 평가 목록
	 */
	private List<PendingEvaluationItemDTO> generatePendingEvaluationsForCustomer(Customer customer) {
		List<PendingEvaluationItemDTO> pendingEvaluations = new ArrayList<>();

		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int currentMonth = now.getMonthValue();

		// FeedbackRequest가 존재하는 연/월 목록 조회
		List<YearMonthProjection> monthsWithFeedback = feedbackRequestRepository
			.findDistinctYearMonthsByCustomerId(customer.getId());

		for (YearMonthProjection ym : monthsWithFeedback) {
			int year = ym.year();
			int month = ym.month();

			// 미래 월 제외
			if (YearMonth.of(year, month).isAfter(YearMonth.of(currentYear, currentMonth))) {
				continue;
			}

			// 월간 평가 확인 (DAY/SWING 모두)
			addMonthlyEvaluationIfPending(customer, year, month, pendingEvaluations);

			// 주간 평가 확인 (DAY 타입만)
			if (customer.getPrimaryInvestmentType() == InvestmentType.DAY) {
				addWeeklyEvaluationsIfPending(customer, year, month, pendingEvaluations);
			}
		}

		return pendingEvaluations;
	}

	/**
	 * 월간 평가가 미작성이면 목록에 추가
	 * (레코드 존재 여부가 아닌 monthlyEvaluation 필드가 채워졌는지 확인)
	 */
	private void addMonthlyEvaluationIfPending(
		Customer customer,
		int year,
		int month,
		List<PendingEvaluationItemDTO> pendingEvaluations
	) {
		// 월간 평가가 실제로 작성되었는지 확인 (monthlyEvaluation IS NOT NULL)
		boolean monthlyEvaluationWritten = monthlyTradingSummaryRepository
			.existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndMonthlyEvaluationIsNotNull(
				customer.getId(),
				year,
				month
			);

		// 미작성이면 추가
		if (!monthlyEvaluationWritten) {
			pendingEvaluations.add(PendingEvaluationItemDTO.monthly(customer, year, month));
		}
	}

	/**
	 * 주간 평가가 미작성이면 목록에 추가
	 * FeedbackRequest가 존재하는 주차만 대상으로 함
	 * (레코드 존재 여부가 아닌 weeklyEvaluation 필드가 채워졌는지 확인)
	 */
	private void addWeeklyEvaluationsIfPending(
		Customer customer,
		int year,
		int month,
		List<PendingEvaluationItemDTO> pendingEvaluations
	) {
		// FeedbackRequest가 존재하는 주차만 조회
		List<Integer> weeksWithFeedback = feedbackRequestRepository
			.findWeeksByCustomerIdAndYearAndMonth(customer.getId(), year, month);

		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int currentMonth = now.getMonthValue();

		// 현재 월인 경우 현재 주차까지만 대상
		int currentWeek = (year == currentYear && month == currentMonth)
			? FeedbackPeriodUtil.resolveFrom(now).week()
			: Integer.MAX_VALUE;

		for (Integer week : weeksWithFeedback) {
			// 미래 주차 제외
			if (week > currentWeek) {
				continue;
			}

			// 주간 평가가 실제로 작성되었는지 확인 (weeklyEvaluation IS NOT NULL)
			boolean weeklyEvaluationWritten = weeklytradingSummaryRepository
				.existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_WeekAndWeeklyEvaluationIsNotNull(
					customer.getId(),
					year,
					month,
					week
				);

			// 미작성이면 추가
			if (!weeklyEvaluationWritten) {
				pendingEvaluations.add(PendingEvaluationItemDTO.weekly(customer, year, month, week));
			}
		}
	}
}
