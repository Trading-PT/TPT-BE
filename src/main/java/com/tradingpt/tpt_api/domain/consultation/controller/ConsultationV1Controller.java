package com.tradingpt.tpt_api.domain.consultation.controller;

import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationCreateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.response.ConsultationResponseDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.response.SlotAvailabilityDTO;
import com.tradingpt.tpt_api.domain.consultation.service.command.ConsultationCommandService;
import com.tradingpt.tpt_api.domain.consultation.service.query.ConsultationQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
@Tag(name = "상담", description = "상담 관련 API (유저용)")
public class ConsultationV1Controller {

    private final ConsultationQueryService consultationQueryService;
    private final ConsultationCommandService consultationCommandService;

    @Operation(
            summary = "특정 날짜의 상담 가능 시간대 조회",
            description = """
                    선택한 날짜의 모든 상담 시간대에 대해 가능한지 여부를 반환합니다.
                    - available: true (예약 가능)
                    - available: false (예약 불가능)
                    """
    )
    @GetMapping("/availability")
    public ResponseEntity<BaseResponse<List<SlotAvailabilityDTO>>> getDailyAvailability(
            @Parameter(description = "조회할 상담 날짜 (예: 2025-08-03)", example = "2025-08-03")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<SlotAvailabilityDTO> result = consultationQueryService.getDailyAvailability(date);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.onSuccess(result));
    }

    @Operation(summary = "상담 예약 생성", description = "특정 날짜와 시간대에 상담 예약을 등록합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createReservation(
            @AuthenticationPrincipal(expression = "id") Long customerId,
            @RequestBody ConsultationCreateRequestDTO request
    ) {

        Long id = consultationCommandService.createReservation(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.onSuccess(id));
    }
    @Operation(summary = "고객별 상담 예약 조회", description = "특정 고객의 상담 예약 목록을 최신순으로 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<ConsultationResponseDTO>>> getByCustomer(
            @AuthenticationPrincipal(expression = "id") Long customerId
    ) {

        List<ConsultationResponseDTO> result = consultationQueryService.getByCustomer(customerId);
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }

    @Operation(summary = "상담 예약 수정", description = "기존 상담을 삭제하고 새로 등록합니다.")
    @PutMapping("/me")
    public ResponseEntity<BaseResponse<Long>> updateReservation(
            @AuthenticationPrincipal(expression = "id") Long customerId,
            @RequestBody ConsultationUpdateRequestDTO request
    ) {
        Long newId = consultationCommandService.updateReservation(customerId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(newId));
    }

    @Operation(summary = "상담 예약 삭제", description = "내 상담 예약을 즉시 삭제합니다.")
    @DeleteMapping("/me/{consultationId}")
    public ResponseEntity<BaseResponse<Long>> deleteReservation(
            @AuthenticationPrincipal(expression = "id") Long customerId,
            @PathVariable Long consultationId
    ) {
        consultationCommandService.deleteReservation(customerId, consultationId);
        return ResponseEntity.ok(BaseResponse.onSuccess(consultationId));
    }

}
