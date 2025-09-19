package com.tradingpt.tpt_api.domain.feedbackresponse.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.service.command.FeedbackResponseCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 피드백 답변 관련 REST API 컨트롤러
 * 트레이너가 피드백 요청에 대해 답변을 생성, 수정하는 기능 제공
 */
@RestController
@RequestMapping("/api/v1/feedback-responses")
@RequiredArgsConstructor
@Tag(name = "피드백 답변", description = "피드백 답변 관련 API")
public class FeedbackResponseV1Controller {

	private final FeedbackResponseCommandService feedbackResponseCommandService;

	@Operation(summary = "피드백 답변 생성", description = "특정 피드백 요청에 대한 답변을 생성합니다. (트레이너만 가능)")
	@PostMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_TRAINER')")
	public BaseResponse<Void> createFeedbackResponse(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@Valid @RequestBody CreateFeedbackResponseRequestDTO request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		feedbackResponseCommandService.createFeedbackResponse(
			feedbackRequestId, request.getResponseContent(), userDetails.getId());

		return BaseResponse.onSuccessCreate(null);
	}

	@Operation(summary = "피드백 답변 수정", description = "피드백 답변을 수정합니다. (답변 작성자만 가능)")
	@PutMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_TRAINER')")
	public BaseResponse<Void> updateFeedbackResponse(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@Valid @RequestBody UpdateFeedbackResponseRequestDTO request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		feedbackResponseCommandService.updateFeedbackResponse(
			feedbackRequestId, request.getResponseContent(), userDetails.getId());

		return BaseResponse.onSuccess(null);
	}
}
