package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "주간 트레이딩 피드백 평가 관리", description = "주간 트레이딩 피드백 평가 관리 API")
public class WeeklyTradingSummaryV1Controller {
}
