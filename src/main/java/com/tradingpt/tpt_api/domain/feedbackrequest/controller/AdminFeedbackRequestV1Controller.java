package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateBestFeedbacksRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.AdminFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.command.FeedbackRequestCommandService;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.query.FeedbackRequestQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/feedback-requests")
@RequiredArgsConstructor
@Tag(name = "어드민 피드백 요청", description = "어드민 피드백 요청 관련 API")
public class AdminFeedbackRequestV1Controller {

	private final FeedbackRequestQueryService feedbackRequestQueryService;
	private final FeedbackRequestCommandService feedbackRequestCommandService;

	@Operation(
		summary = "전체 피드백 목록 조회 (어드민)",
		description = """
			모든 피드백 요청 목록을 조회합니다.
			- 베스트 피드백 최대 3개가 먼저 표시됩니다 (왕관 아이콘)
			- 이후 일반 피드백이 최신순으로 표시됩니다
			- 프론트엔드에서 isBestFeedback=true인 항목을 필터링하여 "현재 선정된 베스트 피드백" 섹션에 표시하세요
			"""
	)
	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<AdminFeedbackResponseDTO> getAllAdminFeedbackRequests(
		@PageableDefault(size = 20) Pageable pageable
	) {
		return BaseResponse.onSuccess(feedbackRequestQueryService.getAdminFeedbackListSlice(pageable));
	}

	@Operation(
		summary = "베스트 피드백 일괄 업데이트 (어드민)",
		description = """
			베스트 피드백을 일괄적으로 업데이트합니다.
			- 기존 베스트 피드백은 모두 해제됩니다
			- 선택된 피드백 ID들이 새로운 베스트로 지정됩니다
			- 최대 3개까지만 선택 가능합니다
			- 빈 배열 전송 시 모든 베스트 피드백이 해제됩니다
			"""
	)
	@PatchMapping("/best")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<Void> updateBestFeedbacks(
		@Valid @RequestBody UpdateBestFeedbacksRequestDTO request
	) {
		return BaseResponse.onSuccess(feedbackRequestCommandService.updateBestFeedbacks(request.getFeedbackIds()));
	}

}
