package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestListItemResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.ScalpingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SwingFeedbackRequestDetailResponseDTO;
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
	public BaseResponse<DayFeedbackRequestDetailResponseDTO> createDayRequest(
		@Valid @ModelAttribute CreateDayRequestDetailRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createDayRequest(request, customerId));
	}

	@Operation(summary = "스켈핑 피드백 요청 생성", description = "스켈핑 피드백 요청을 생성합니다.")
	@PostMapping(value = "/scalping", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<ScalpingFeedbackRequestDetailResponseDTO> createScalpingRequest(
		@Valid @ModelAttribute CreateScalpingRequestDetailRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createScalpingRequest(request, customerId));
	}

	@Operation(summary = "스윙 피드백 요청 생성", description = "스윙 피드백 요청을 생성합니다.")
	@PostMapping(value = "/swing", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<SwingFeedbackRequestDetailResponseDTO> createSwingRequest(
		@Valid @ModelAttribute CreateSwingRequestDetailRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createSwingRequest(request, customerId));
	}

	@Operation(
		summary = "실시간 트레이딩 목록 조회 (무한 스크롤)",
		description = """
			모든 고객의 피드백 요청 목록을 조회합니다.
			- 베스트 피드백이 먼저 표시됩니다 (왕관 아이콘)
			- 각 그룹 내에서 최신순으로 정렬됩니다
			- page: 페이지 번호 (0부터 시작, 기본값: 0)
			- size: 페이지 크기 (기본값: 12)
			- 구독 고객: 모든 피드백 요청 조회 가능
			- 미구독 고객: 베스트 피드백만 조회 가능
			"""
	)
	@GetMapping
	public BaseResponse<FeedbackListResponseDTO> getFeedbackList(
		@PageableDefault(size = 12) Pageable pageable
	) {
		return BaseResponse.onSuccess(feedbackRequestQueryService.getFeedbackListSlice(pageable));
	}

	@Operation(summary = "피드백 요청 상세 조회", description = """
		특정 피드백 요청의 상세 정보를 조회합니다.
		
		접근 권한:
		- 자신이 작성한 피드백
		- 구독 중인 사용자는 모든 피드백 조회 가능
		
		미구독 사용자는 자신의 피드백만 조회 가능합니다.
		""")
	@GetMapping("/{feedbackRequestId}")
	public BaseResponse<FeedbackRequestDetailResponseDTO> getFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getFeedbackRequestById(feedbackRequestId, customerId)
		);
	}

	@Operation(summary = "피드백 요청 삭제", description = "피드백 요청을 삭제합니다. (고객만 가능)")
	@DeleteMapping("/{feedbackRequestId}")
	public BaseResponse<Void> deleteFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccessDelete(
			feedbackRequestCommandService.deleteFeedbackRequest(feedbackRequestId, customerId));
	}

	@Operation(
		summary = "특정 날짜의 피드백 요청 목록 조회",
		description = """
			특정 날짜에 작성된 고객의 모든 피드백 요청을 시간순으로 조회합니다.
			
			특징:
			- 완강 여부, 트레이딩 유형에 상관없이 모든 피드백 표시
			- 작성 시간 오름차순 정렬 (오래된 것부터)
			- 목록에서 각 항목을 클릭하면 상세 조회 API 호출
			
			사용 시나리오:
			- 주간 요약에서 특정 날짜 클릭 시 호출
			- 해당 날짜의 모든 피드백 요청을 시간순으로 확인
			
			날짜 검증:
			- 연도: 2020~2100
			- 월: 1~12
			- 일: 해당 월의 유효한 날짜 (예: 2월은 28/29일까지)
			"""
	)
	@GetMapping("/customers/me/years/{year}/months/{month}/days/{day}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<List<FeedbackRequestListItemResponseDTO>> getDailyFeedbackRequests(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월", example = "8", required = true)
		@PathVariable Integer month,
		@Parameter(description = "일", example = "24", required = true)
		@PathVariable Integer day,
		@AuthenticationPrincipal(expression = "id") Long customerId

	) {

		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getDailyFeedbackRequests(customerId, year, month, day));
	}
}
