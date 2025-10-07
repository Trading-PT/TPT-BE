package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "주간 매매 일지 통계", description = "피드백 요청 주간 매매 일지 통계 API")
public class WeeklyTradingSummaryV1Controller {

}
