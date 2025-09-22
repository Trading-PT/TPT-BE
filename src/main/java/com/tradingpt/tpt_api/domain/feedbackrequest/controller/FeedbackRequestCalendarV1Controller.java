package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.query.FeedbackRequestCalendarQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedback-requests/calendar")
@RequiredArgsConstructor
@Tag(name = "피드백 요청 캘린더", description = "피드백 요청 캘린더 관련 API")
public class FeedbackRequestCalendarV1Controller {

	private final FeedbackRequestCalendarQueryService feedbackRequestCalendarQueryService;

	@Operation(summary = "해당 연도에 대한 월별 피드백 요청 리스트"
		, description = "해당 연도에 대한 피드백 요청이 존재하는 월을 리스트업 합니다.")
	@GetMapping(value = "/year/{year}")
	public BaseResponse<MonthlySummaryResponseDTO> getMonthlySummaryResponse(
		@PathVariable Integer year,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return BaseResponse.onSuccess(
			feedbackRequestCalendarQueryService.getMonthlySummaryResponse(year, userDetails.getId()));
	}
}
