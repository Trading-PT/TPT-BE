package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DailyFeedbackRequestsResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.MonthlyFeedbackSummaryResult;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentHistory;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentHistoryRepository;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestCalendarQueryServiceImpl implements FeedbackRequestCalendarQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final InvestmentHistoryRepository investmentHistoryRepository;

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
	public DailyFeedbackRequestsResponseDTO getDailyFeedbackRequestsResponse(Integer year, Integer month, Integer day,
		Long customerId) {
		LocalDate feedbackDate = LocalDate.of(year, month, day);

		List<FeedbackRequest> feedbackRequests = feedbackRequestRepository
			.findFeedbackRequestsByCustomerAndDate(customerId, feedbackDate);

		List<DailyFeedbackRequestsResponseDTO.DailyFeedbackRequestSummaryDTO> summaries = new ArrayList<>(
			feedbackRequests.size());
		int dailySequence = 1;
		for (FeedbackRequest feedbackRequest : feedbackRequests) {
			summaries.add(
				DailyFeedbackRequestsResponseDTO.DailyFeedbackRequestSummaryDTO.of(feedbackRequest, dailySequence++));
		}

		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(feedbackDate);
		InvestmentType investmentType = investmentHistoryRepository
			.findActiveHistory(customerId, feedbackDate)
			.map(InvestmentHistory::getInvestmentType)
			.orElseGet(() -> feedbackRequests.isEmpty() ? null :
				feedbackRequests.get(0).getCustomer().getInvestmentTypeOn(feedbackDate));

		return DailyFeedbackRequestsResponseDTO.of(
			feedbackDate,
			period.year(),
			period.month(),
			period.week(),
			investmentType,
			summaries
		);
	}

	@Override
	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId) {
		return null;
	}
}
