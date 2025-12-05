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

import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationListResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
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
	private final TrainerRepository trainerRepository;
	private final UserRepository userRepository;

	@Override
	public PendingEvaluationListResponseDTO getPendingEvaluations(Long userId, Pageable pageable) {
		// 1. 사용자 조회 및 역할 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 2. Role에 따라 다른 조회 로직 실행
		Slice<Customer> customerSlice;
		if (user.getRole() == Role.ROLE_ADMIN) {
			// ADMIN: 모든 완강 고객 조회
			customerSlice = customerRepository.findByCourseStatusOrderByNameAsc(
				CourseStatus.AFTER_COMPLETION,
				pageable
			);
			log.info("Admin user (ID: {}) fetching all pending evaluations", userId);
		} else {
			// TRAINER: 담당 고객만 조회
			customerSlice = customerRepository.findByAssignedTrainerIdAndCourseStatusOrderByNameAsc(
				userId,
				CourseStatus.AFTER_COMPLETION,
				pageable
			);
			log.info("Trainer (ID: {}) fetching assigned customers' pending evaluations", userId);
		}

		// 3. 조회된 고객들의 미작성 평가 목록 생성
		List<PendingEvaluationItemDTO> allPendingEvaluations = new ArrayList<>();

		for (Customer customer : customerSlice.getContent()) {
			// 완강 시점이 없으면 스킵 (데이터 정합성 보호)
			if (customer.getCompletedAt() == null) {
				continue;
			}

			// 고객별 미작성 평가 목록 생성 및 추가
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
	 * 특정 고객의 완강 월부터 현재 월까지의 미작성 평가 목록 생성
	 *
	 * @param customer 고객 엔티티
	 * @return 미작성 평가 목록
	 */
	private List<PendingEvaluationItemDTO> generatePendingEvaluationsForCustomer(Customer customer) {
		List<PendingEvaluationItemDTO> pendingEvaluations = new ArrayList<>();

		// 완강 월 계산
		YearMonth completedYearMonth = YearMonth.from(customer.getCompletedAt());
		int completedYear = completedYearMonth.getYear();
		int completedMonth = completedYearMonth.getMonthValue();

		// 현재 연/월
		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int currentMonth = now.getMonthValue();

		// 완강 월부터 현재 월까지 순회
		YearMonth startYearMonth = YearMonth.of(completedYear, completedMonth);
		YearMonth endYearMonth = YearMonth.of(currentYear, currentMonth);

		for (YearMonth yearMonth = startYearMonth;
			 !yearMonth.isAfter(endYearMonth);
			 yearMonth = yearMonth.plusMonths(1)) {

			int year = yearMonth.getYear();
			int month = yearMonth.getMonthValue();

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
	 */
	private void addMonthlyEvaluationIfPending(
		Customer customer,
		int year,
		int month,
		List<PendingEvaluationItemDTO> pendingEvaluations
	) {
		// 월간 평가 존재 여부 확인
		boolean monthlyExists = monthlyTradingSummaryRepository.existsByCustomer_IdAndPeriod_YearAndPeriod_Month(
			customer.getId(),
			year,
			month
		);

		// 미작성이면 추가
		if (!monthlyExists) {
			pendingEvaluations.add(PendingEvaluationItemDTO.monthly(customer, year, month));
		}
	}

	/**
	 * 주간 평가가 미작성이면 목록에 추가
	 * 현재 월인 경우 현재 주차까지만, 과거 월인 경우 전체 주차
	 */
	private void addWeeklyEvaluationsIfPending(
		Customer customer,
		int year,
		int month,
		List<PendingEvaluationItemDTO> pendingEvaluations
	) {
		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int currentMonth = now.getMonthValue();

		// 해당 월의 최대 주차 계산
		int maxWeek;
		if (year == currentYear && month == currentMonth) {
			// 현재 월: 현재 주차까지만
			maxWeek = FeedbackPeriodUtil.resolveFrom(now).week();
		} else {
			// 과거 월: 해당 월의 전체 주차
			maxWeek = FeedbackPeriodUtil.getWeeksInMonth(year, month);
		}

		// 1주차부터 maxWeek까지 순회
		for (int week = 1; week <= maxWeek; week++) {
			// 주간 평가 존재 여부 확인
			boolean weeklyExists = weeklytradingSummaryRepository.existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
				customer.getId(),
				year,
				month,
				week
			);

			// 미작성이면 추가
			if (!weeklyExists) {
				pendingEvaluations.add(PendingEvaluationItemDTO.weekly(customer, year, month, week));
			}
		}
	}
}
