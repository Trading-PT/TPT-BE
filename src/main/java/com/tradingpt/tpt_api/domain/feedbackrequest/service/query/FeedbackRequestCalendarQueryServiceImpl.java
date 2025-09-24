package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DailyFeedbackRequestsResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestCalendarQueryServiceImpl implements FeedbackRequestCalendarQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;

	@Override
	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Long customerId) {
		return null;
	}

	@Override
	public DailyFeedbackRequestsResponseDTO getDailyFeedbackRequestsResponse(Integer year, Integer month, Integer day, Long customerId) {
		LocalDate feedbackDate = LocalDate.of(year, month, day);

		List<FeedbackRequest> feedbackRequests = feedbackRequestRepository
			.findFeedbackRequestsByCustomerAndDate(customerId, feedbackDate);

		List<DailyFeedbackRequestsResponseDTO.DailyFeedbackRequestSummaryDTO> summaries = new ArrayList<>(feedbackRequests.size());
		int dailySequence = 1;
		for (FeedbackRequest feedbackRequest : feedbackRequests) {
			summaries.add(DailyFeedbackRequestsResponseDTO.DailyFeedbackRequestSummaryDTO.of(feedbackRequest, dailySequence++));
		}

		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(feedbackDate);

		return DailyFeedbackRequestsResponseDTO.of(
			feedbackDate,
			period.year(),
			period.month(),
			period.week(),
			summaries
		);
	}
}
