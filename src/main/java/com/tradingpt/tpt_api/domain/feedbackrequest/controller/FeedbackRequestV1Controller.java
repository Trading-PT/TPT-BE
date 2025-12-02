package com.tradingpt.tpt_api.domain.feedbackrequest.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestListItemResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlyPnlCalendarResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TrainerWrittenFeedbackListResponseDTO;
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

	@Operation(
		summary = "피드백 요청 생성 (DAY/SWING 통합)",
		description = """
			피드백 요청을 생성합니다.
			
			투자 유형:
			- DAY: 데이 트레이딩
			- SWING: 스윙 트레이딩
			
			investmentType 필드로 유형을 구분하며, SWING 유형의 경우
			positionStartDate, positionEndDate 필드가 추가로 필요합니다.
			"""
	)
	@PostMapping(consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<FeedbackRequestDetailResponseDTO> createFeedbackRequest(
		@Valid @ModelAttribute CreateFeedbackRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccessCreate(
			feedbackRequestCommandService.createFeedbackRequest(request, customerId));
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

	@Operation(
		summary = "피드백 요청 상세 조회",
		description = """
			특정 피드백 요청의 상세 정보를 조회합니다.
			
			접근 권한:
			- 베스트 피드백/트레이너 작성 피드백: 누구나 조회 가능
			- 일반 피드백: 로그인 필수
			  - 구독 중인 사용자: 모든 피드백 조회 가능
			  - 미구독 사용자: 자신의 피드백만 조회 가능
			"""
	)
	@GetMapping("/{feedbackRequestId}")
	public BaseResponse<FeedbackRequestDetailResponseDTO> getFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		Authentication authentication) {

		// 인증된 사용자의 ID 추출 (익명 사용자는 null)
		Long customerId = null;
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof AuthSessionUser sessionUser) {
				// 정상 로그인 사용자 (JSON 로그인, OAuth2 로그인)
				customerId = sessionUser.id();
			} else if (principal instanceof CustomUserDetails userDetails) {
				// Remember-me로 복원된 사용자
				customerId = userDetails.getId();
			}
			// String principal (익명 사용자 "anonymousUser")은 무시 → customerId = null
		}

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
		summary = "피드백 요청 수정",
		description = """
			자신이 작성한 피드백 요청을 수정합니다.
			
			수정 가능 조건:
			- 자신이 작성한 피드백만 수정 가능
			- 트레이너 피드백 답변이 완료되지 않은 경우에만 수정 가능
			
			수정 불가 필드 (생성 시 결정):
			- investmentType: 투자 타입 (DAY/SWING)
			- courseStatus: 완강 여부
			- feedbackYear, feedbackMonth, feedbackWeek: 날짜 정보
			
			수정 가능 필드:
			- 매매 기본 정보: 종목, 포지션, P&L, 손익비, 레버리지 등
			- 매매 상세 정보: 진입/탈출 가격, 손절가, 익절가, 복기 내용 등
			- 완강 후 전용 필드: 디렉션 프레임, 메인 프레임, 추세 분석 등
			- SWING 전용 필드: 포지션 시작/종료 날짜
			"""
	)
	@PutMapping("/{feedbackRequestId}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<FeedbackRequestDetailResponseDTO> updateFeedbackRequest(
		@Parameter(description = "피드백 요청 ID") @PathVariable Long feedbackRequestId,
		@Valid @RequestBody UpdateFeedbackRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId) {

		return BaseResponse.onSuccess(
			feedbackRequestCommandService.updateFeedbackRequest(feedbackRequestId, request, customerId));
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

	@Operation(
		summary = "월별 PnL 달력 조회",
		description = """
			특정 연/월의 모든 피드백 요청에 대한 PnL을 달력 형태로 조회합니다.
			
			특징:
			- 일별로 그룹핑하여 PnL 합계와 평균 퍼센테이지 제공
			- 모든 투자 유형(DAY, SWING) 포함
			- 하루에 여러 피드백이 있으면 합산하여 표시
			- 월 전체 통계(총 PnL, 평균 퍼센테이지) 제공
			
			사용 시나리오:
			- 달력 UI에서 각 날짜별 수익률 표시
			- 초록색(수익), 빨간색(손실)로 시각화
			- 날짜 클릭 시 해당 일의 상세 피드백 목록 조회
			
			예시:
			- GET /api/v1/feedback-requests/customers/me/years/2025/months/9/pnl-calendar
			"""
	)
	@GetMapping("/customers/me/years/{year}/months/{month}/pnl-calendar")
	public BaseResponse<MonthlyPnlCalendarResponseDTO> getMonthlyPnlCalendar(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "9", required = true)
		@PathVariable Integer month,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getMonthlyPnlCalendar(customerId, year, month)
		);
	}

	@Operation(
		summary = "트레이너 작성 매매일지 조회 (무한 스크롤)",
		description = """
			트레이너가 직접 작성한 매매일지 목록을 조회합니다.
			
			특징:
			- isTrainerWritten = true인 피드백만 조회
			- 모든 투자 유형(DAY, SWING) 포함
			- 첨부 이미지, 제목, 매매 복기 포함
			- 최신순 정렬 (createdAt DESC)
			- 무한 스크롤 지원 (Slice 기반)
			
			사용 시나리오:
			- 트레이너의 실제 매매 일지 학습 자료로 활용
			- 썸네일 이미지와 함께 리스트 표시
			- 클릭 시 상세 조회 API로 이동
			
			예시:
			- GET /api/v1/feedback-requests/trainer-written?page=0&size=12
			"""
	)
	@GetMapping("/trainer-written")
	public BaseResponse<TrainerWrittenFeedbackListResponseDTO> getTrainerWrittenFeedbacks(
		@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return BaseResponse.onSuccess(
			feedbackRequestQueryService.getTrainerWrittenFeedbacks(pageable)
		);
	}
}
