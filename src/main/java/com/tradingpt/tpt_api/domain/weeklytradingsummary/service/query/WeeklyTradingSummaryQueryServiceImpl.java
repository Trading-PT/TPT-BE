package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyTradingSummaryQueryServiceImpl implements WeeklyTradingSummaryQueryService {

	@Override
	public WeeklySummaryResponseDTO getWeeklyTradingSummary(Integer year, Integer month, Integer week,
		Long customerId) {
		return null;
	}
}
