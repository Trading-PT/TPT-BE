package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponse;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.command.FeedbackRequestCommandService;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.query.FeedbackRequestQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedback-requests")
@RequiredArgsConstructor
@Tag(name = "피드백 요청", description = "피드백 요청 관련 API")
public class FeedbackRequestV1Controller {

	private final FeedbackRequestCommandService feedbackRequestCommandService;
	private final FeedbackRequestQueryService feedbackRequestQueryService;

	@Operation(summary = "데이 트레이딩 피드백 요청 생성", description = "데이 트레이딩 피드백 요청을 생성합니다.")
	@PostMapping(value = "/day", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<FeedbackRequestResponse> createDayRequest(
		@Valid @ModelAttribute CreateDayRequestDetailRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createDayRequest(request, userDetails.getId()));
	}

	@Operation(summary = "스켈핑 피드백 요청 생성", description = "스켈핑 피드백 요청을 생성합니다.")
	@PostMapping(value = "/scalping", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<FeedbackRequestResponse> createScalpingRequest(
		@Valid @ModelAttribute CreateScalpingRequestDetailRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createScalpingRequest(request, userDetails.getId()));
	}

	@Operation(summary = "스윙 피드백 요청 생성", description = "스윙 피드백 요청을 생성합니다.")
	@PostMapping(value = "/swing", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<FeedbackRequestResponse> createSwingRequest(
		@Valid @ModelAttribute CreateSwingRequestDetailRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createSwingRequest(request, userDetails.getId()));
	}

	@Operation(summary = "피드백 요청 목록 조회", description = "피드백 요청 목록을 페이징으로 조회합니다.")
	@GetMapping
	@PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_TRAINER')")
	public BaseResponse<Page<FeedbackRequestResponse>> getFeedbackRequests(
		@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@Parameter(description = "피드백 타입 필터") @RequestParam(required = false) FeedbackType feedbackType,
		@Parameter(description = "상태 필터") @RequestParam(required = false) Status status,
		@Parameter(description = "고객 ID 필터 (트레이너만 사용 가능)") @RequestParam(required = false) Long customerId) {

		return BaseResponse.onSuccess(feedbackRequestQueryService.getFeedbackRequests(
			pageable, feedbackType, status, customerId));
	}

	@Operation(summary = "피드백 요청 상세 조회", description = "특정 피드백 요청의 상세 정보를 조회합니다.")
	@GetMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_TRAINER')")
	public BaseResponse<FeedbackRequestResponse> getFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccess(feedbackRequestQueryService.getFeedbackRequestById(
			feedbackRequestId, userDetails.getId()));
	}

	@Operation(summary = "피드백 요청 삭제", description = "피드백 요청을 삭제합니다. (고객만 가능)")
	@DeleteMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public BaseResponse<Void> deleteFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccessDelete(
			feedbackRequestCommandService.deleteFeedbackRequest(feedbackRequestId, userDetails.getId()));
	}

	@Operation(summary = "내 피드백 요청 목록 조회", description = "현재 로그인한 고객의 피드백 요청 목록을 조회합니다.")
	@GetMapping("/my")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<List<FeedbackRequestResponse>> getMyFeedbackRequests(
		@Parameter(description = "피드백 타입 필터") @RequestParam(required = false) FeedbackType feedbackType,
		@Parameter(description = "상태 필터") @RequestParam(required = false) Status status,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return BaseResponse.onSuccess(feedbackRequestQueryService.getMyFeedbackRequests(
			userDetails.getId(), feedbackType, status));
	}

}
