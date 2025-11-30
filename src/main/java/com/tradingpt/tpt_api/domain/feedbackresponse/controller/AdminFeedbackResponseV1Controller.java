package com.tradingpt.tpt_api.domain.feedbackresponse.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.CreateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.UpdateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.service.command.FeedbackResponseCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 피드백 답변 관련 REST API 컨트롤러.
 * 트레이너 및 관리자가 피드백 요청에 대한 답변을 작성/수정할 수 있습니다.
 */
@RestController
@RequestMapping("/api/v1/admin/feedback-responses")
@RequiredArgsConstructor
@Tag(name = "피드백 답변 (Admin)", description = """
	피드백 답변 관련 API

	## 권한
	- **ROLE_ADMIN**: 모든 피드백 요청에 답변 가능
	- **ROLE_TRAINER**: 토큰 사용 피드백 또는 본인에게 배정된 고객의 피드백에만 답변 가능

	## 답변 권한 규칙
	1. **Admin**: 모든 피드백 요청에 답변 가능
	2. **Trainer + 토큰 사용 피드백**: 모든 트레이너 응답 가능
	3. **Trainer + 일반 피드백**: 해당 고객에게 배정된 트레이너만 응답 가능
	""")
public class AdminFeedbackResponseV1Controller {

	private final FeedbackResponseCommandService feedbackResponseCommandService;

	@Operation(
		summary = "피드백 답변 생성",
		description = """
			특정 피드백 요청에 대한 답변을 생성합니다.

			## 권한
			- **ROLE_ADMIN**: 모든 피드백 요청에 답변 가능
			- **ROLE_TRAINER**: 조건부 답변 가능 (토큰 사용 피드백 또는 본인 배정 고객)

			## 주의사항
			- 이미 답변이 존재하는 피드백 요청에는 답변을 생성할 수 없습니다.
			- 답변 생성 시 피드백 상태가 FN(응답 완료)으로 변경됩니다.
			"""
	)
	@PostMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_ADMIN')")
	public BaseResponse<FeedbackResponseDTO> createFeedbackResponse(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@Valid @RequestBody CreateFeedbackResponseRequestDTO request,
		@Parameter(hidden = true) @AuthenticationPrincipal(expression = "id") Long writerId) {

		return BaseResponse.onSuccessCreate(feedbackResponseCommandService.createFeedbackResponse(
			feedbackRequestId, request, writerId));
	}

	@Operation(
		summary = "피드백 답변 수정",
		description = """
			피드백 답변을 수정합니다.

			## 권한
			- **답변 작성자만 수정 가능**: 본인이 작성한 답변만 수정할 수 있습니다.
			- Admin이 작성한 답변은 해당 Admin만, Trainer가 작성한 답변은 해당 Trainer만 수정 가능
			"""
	)
	@PutMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_ADMIN')")
	public BaseResponse<FeedbackResponseDTO> updateFeedbackResponse(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@Valid @RequestBody UpdateFeedbackResponseRequestDTO request,
		@Parameter(hidden = true) @AuthenticationPrincipal(expression = "id") Long writerId) {

		return BaseResponse.onSuccess(feedbackResponseCommandService.updateFeedbackResponse(
			feedbackRequestId, request, writerId));
	}
}
