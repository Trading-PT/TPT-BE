package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyTradingSummaryQueryServiceImpl implements WeeklyTradingSummaryQueryService {

	private final UserRepository userRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final WeeklyTradingSummaryRepository weeklyTradingSummaryRepository;

	@Override
	public WeeklySummaryResponseDTO getWeeklyTradingSummary(Integer year, Integer month, Integer week,
		Long customerId) {

		// 고객 확인
		Customer customer = (Customer)userRepository.findById(customerId)
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
			if (investmentType == InvestmentType.DAY) {
				return buildAfterCompletionDaySummary(customerId, year, month, courseStatus, investmentType);
			} else {
				return buildAfterCompletionGeneralSummary(customerId, year, month, courseStatus, investmentType);
			}
		}

	}

	private WeeklySummaryResponseDTO buildBeforeCompletionSummary(Long customerId, Integer year, Integer month,
		CourseStatus courseStatus, InvestmentType investmentType) {
		return null;
	}

	private WeeklySummaryResponseDTO buildAfterCompletionDaySummary(Long customerId, Integer year, Integer month,
		CourseStatus courseStatus, InvestmentType investmentType) {
		return null;
	}

	private WeeklySummaryResponseDTO buildAfterCompletionGeneralSummary(Long customerId, Integer year, Integer month,
		CourseStatus courseStatus, InvestmentType investmentType) {
		return null;
	}
}
