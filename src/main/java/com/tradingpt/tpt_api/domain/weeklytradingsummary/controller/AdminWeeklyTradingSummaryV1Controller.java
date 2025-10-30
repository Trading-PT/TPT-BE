package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DailyFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyDayFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command.WeeklyTradingSummaryCommandService;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query.WeeklyTradingSummaryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "ADMIN 주간 매매 일지 통계", description = "ADMIN이 사용하는 주간 매매 일지 통계 API")
public class AdminWeeklyTradingSummaryV1Controller {

	private final WeeklyTradingSummaryQueryService weeklyTradingSummaryQueryService;
	private final WeeklyTradingSummaryCommandService weeklyTradingSummaryCommandService;

	@Operation(
		summary = "주간 매매 일지 통계 작성 (Trainer)",
		description = """
			트레이너가 주간 매매 일지 통계를 작성합니다.
			
			⭐ 작성 규칙:
			
			1. 완강 전 (BEFORE_COMPLETION):
			   🚫 트레이너는 작성 불가 (고객이 memo 작성)
			
			2. 완강 후 (AFTER_COMPLETION) + DAY 유형:
			   ❌ memo: 불가
			   ✅ weeklyEvaluation: 필수
			   ✅ weeklyProfitableTradingAnalysis: 필수
			   ✅ weeklyLossTradingAnalysis: 필수
			
			3. 완강 후 (AFTER_COMPLETION) + SCALPING/SWING 유형:
			   🚫 작성 불가
			
			제약 조건:
			- 해당 주의 코스 상태와 투자 타입은 첫 번째 피드백 기준
			- 이미 해당 주에 통계가 존재하면 생성 불가
			"""
	)
	@PostMapping("/customers/{customerId}/years/{year}/months/{month}/weeks/{week}")
	public BaseResponse<Void> createWeeklySummaryByTrainer(
		@Parameter(description = "고객 ID", required = true)
		@PathVariable Long customerId,
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "8", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@Valid @RequestBody CreateWeeklyTradingSummaryRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(
			weeklyTradingSummaryCommandService.createWeeklyTradingSummaryByTrainer(
				year, month, week, customerId, trainerId, request)
		);
	}

	@Operation(
		summary = "특정 주의 피드백이 존재하는 날짜 목록 조회",
		description = """
			특정 연/월/주에 피드백 요청이 존재하는 날짜(일) 목록을 조회합니다.
			
			특징:
			- 내 담당 고객의 데이터만 조회 가능
			- 피드백 요청이 있는 날짜만 반환
			- 오름차순 정렬
			- 모든 투자 유형(DAY, SCALPING, SWING) 포함
			
			사용 시나리오:
			- 화면에서 "셋째 주"를 선택했을 때
			- 해당 주에 피드백이 있는 날짜들을 표시 (17일, 19일, 21일, 22일)
			
			예시:
			- 2025년 7월 셋째 주(3주차)에 17일, 19일, 21일, 22일에 피드백이 있다면
			- days: [17, 19, 21, 22] 반환
			"""
	)
	@GetMapping("/customers/{customerId}/years/{year}/months/{month}/weeks/{week}/days")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<WeeklyDayFeedbackResponseDTO> getWeeklyDayFeedback(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "7", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@Parameter(description = "고객 ID", required = true)
		@PathVariable Long customerId,
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(
			weeklyTradingSummaryQueryService.getWeeklyDayFeedback(
				year, month, week, customerId, trainerId)
		);
	}

	@Operation(
		summary = "특정 날짜의 피드백 목록 조회",
		description = """
			특정 연/월/주/일에 해당하는 모든 피드백 요청 목록을 조회합니다.
			
			특징:
			- 내 담당 고객의 데이터만 조회 가능
			- 해당 날짜의 모든 피드백 반환
			- 최신순 정렬
			- 모든 투자 유형(DAY, SCALPING, SWING) 포함
			- 피드백 응답 여부 포함
			
			사용 시나리오:
			- 화면에서 "22일"을 클릭했을 때
			- 22일의 모든 피드백 목록 표시
			- 각 피드백 클릭 시 상세 화면으로 이동
			
			예시:
			- 2025년 7월 셋째 주 22일의 피드백들
			- 피드백 목록과 각 피드백의 상세내역 조회 가능
			"""
	)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	@GetMapping("/customers/{customerId}/years/{year}/months/{month}/weeks/{week}/days/{day}")
	public BaseResponse<DailyFeedbackListResponseDTO> getDailyFeedbackList(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "7", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@Parameter(description = "일", example = "22", required = true)
		@PathVariable Integer day,
		@Parameter(description = "고객 ID", required = true)
		@PathVariable Long customerId,
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(
			weeklyTradingSummaryQueryService.getDailyFeedbackList(
				year, month, week, day, customerId, trainerId)
		);
	}
}
