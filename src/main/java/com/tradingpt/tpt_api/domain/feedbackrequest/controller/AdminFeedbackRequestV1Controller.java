package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateBestFeedbacksRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.AdminFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MyCustomerNewFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TokenUsedFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.command.FeedbackRequestCommandService;
import com.tradingpt.tpt_api.domain.feedbackrequest.service.query.FeedbackRequestQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
		summary = "베스트 피드백 관리 (어드민)",
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

	@Operation(
		summary = "토큰 사용 피드백 요청 목록 조회 (무한 스크롤)",
		description = """
			토큰을 사용하여 작성된 피드백 요청 목록을 무한 스크롤 방식으로 조회합니다.
			
			특징:
			- BASIC 멤버십 고객이 토큰을 사용한 피드백만 조회
			- 최신순 정렬
			- 응답 여부 확인 가능 (status 필드)
			- 응답한 트레이너 정보 포함
			- Slice 기반 무한 스크롤 지원 (hasNext로 다음 페이지 여부 판단)
			
			사용 시나리오:
			- 트레이너가 응답 가능한 피드백 목록 확인 (무한 스크롤)
			- 어드민이 토큰 사용 현황 모니터링
			
			페이징 파라미터:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 12)
			
			예시:
			- GET /api/v1/admin/feedback-requests/token-used
			- GET /api/v1/admin/feedback-requests/token-used?page=0&size=12
			- GET /api/v1/admin/feedback-requests/token-used?page=1&size=12
			"""
	)
	@GetMapping("/token-used")
	public BaseResponse<TokenUsedFeedbackListResponseDTO> getTokenUsedFeedbackRequests(
		@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getTokenUsedFeedbackRequests(pageable)
		);
	}

	@Operation(
		summary = "내 담당 고객의 새로운 피드백 요청 리스트 (무한 스크롤)",
		description = """
			내 담당 고객들의 새로운 피드백 요청 목록을 무한 스크롤 방식으로 조회합니다.
			
			특징:
			- 트레이너가 담당하는 고객들의 피드백만 조회
			- status가 N (피드백 대기)인 것만 조회
			- 최신순 정렬
			- 모든 투자 유형(DAY, SCALPING, SWING) 포함
			- Slice 기반 무한 스크롤 지원
			
			사용 시나리오:
			- 트레이너가 아직 응답하지 않은 피드백 확인
			- 담당 고객의 새로운 피드백 알림 표시
			- 우선 순위로 응답할 피드백 파악
			
			페이징 파라미터:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 12)
			
			예시:
			- GET /api/v1/admin/feedback-requests/my-customers/new
			- GET /api/v1/admin/feedback-requests/my-customers/new?page=0&size=12
			- GET /api/v1/admin/feedback-requests/my-customers/new?page=1&size=12
			"""
	)
	@GetMapping("/my-customers/new")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<MyCustomerNewFeedbackListResponseDTO> getMyCustomerNewFeedbackRequests(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable

	) {
		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getMyCustomerNewFeedbackRequests(trainerId, pageable)
		);
	}

	@Operation(
		summary = "피드백 요청 상세 조회 (어드민)",
		description = """
			특정 피드백 요청의 상세 정보를 조회합니다.
			
			특징:
			- 투자 유형별 상세 정보 포함 (DAY/SCALPING/SWING)
			- 고객 정보 및 트레이너 응답 내용 포함
			- 매매 내역, 손익 정보, 차트 이미지 등 모든 데이터 조회
			
			사용 시나리오:
			- 트레이너가 피드백 응답 작성 시
			- 어드민이 피드백 내용 확인 시
			- 베스트 피드백 선정 시 상세 내용 검토
			
			예시:
			- GET /api/v1/admin/feedback-requests/123
			"""
	)
	@GetMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<FeedbackRequestDetailResponseDTO> getFeedbackRequestDetail(
		@Parameter(description = "피드백 요청 ID", required = true)
		@PathVariable Long feedbackRequestId
	) {
		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getAdminFeedbackDetail(feedbackRequestId)
		);
	}

	// TODO: 트레이너 작성 매매일지 플래그 업데이트 API 구현 필요
	// TODO: POST /api/v1/admin/feedback-requests/{id}/trainer-written
	// TODO: 현재는 DataGrip으로 수동 업데이트 중이나, 향후 Admin UI에서 직접 설정할 수 있도록 구현 예정
	// TODO: 베스트 피드백 설정 API (updateBestFeedbackStatus)와 유사한 구조로 구현
	// TODO: Request DTO: { isTrainerWritten: boolean }
	// TODO: 권한: ADMIN 또는 TRAINER
	// TODO: 검증: 해당 피드백이 실제 트레이너가 작성한 것인지 확인하는 로직 추가 고려
	// TODO: (현재는 수동으로 확인 후 플래그만 설정하지만, 향후 자동 검증 로직 추가 가능)
}
