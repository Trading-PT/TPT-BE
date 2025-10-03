package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DailyFeedbackRequestsResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentTypeHistory;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenthistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenthistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResult;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestJournalQueryServiceImpl implements FeedbackRequestJournalQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepositoryReadOnly;

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
	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId) {
		InvestmentTypeHistory customerUserInvestmentType = investmentTypeHistoryRepositoryReadOnly.findActiveHistory(
				customerId,
				LocalDate.of(year, month, 1))
			.orElseThrow(
				() -> new InvestmentHistoryException(InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_NOT_FOUND));

		return null;
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
		InvestmentType investmentType = investmentTypeHistoryRepository
			.findActiveHistory(customerId, feedbackDate)
			.map(InvestmentTypeHistory::getInvestmentType)
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

}
