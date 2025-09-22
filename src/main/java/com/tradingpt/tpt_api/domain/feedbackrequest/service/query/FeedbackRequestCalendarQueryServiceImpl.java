package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlySummaryResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestCalendarQueryServiceImpl implements FeedbackRequestCalendarQueryService {
	
	@Override
	public MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Long customerId) {
		return null;
	}
}
