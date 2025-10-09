package com.tradingpt.tpt_api.domain.consultation.controller;

import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationBlockUpsertRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationMemoUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.response.AdminConsultationResponseDTO;
import com.tradingpt.tpt_api.domain.consultation.service.command.AdminConsultationCommandService;
import com.tradingpt.tpt_api.domain.consultation.service.query.AdminConsultationQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/consultations")
@RequiredArgsConstructor
@Validated
@Tag(name = "상담(어드민)", description = "상담 관련 API (어드민용)")
public class AdminConsultationV1Controller {

    private final AdminConsultationQueryService queryService;
    private final AdminConsultationCommandService commandService;


    @Operation(summary = "상담 목록(관리자) 조회",
            description = "processed: ALL|TRUE|FALSE (기본 ALL). 예) ?processed=FALSE&page=0&size=20&sort=createdAt,desc"
    )
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminConsultationResponseDTO>>> getConsultations(
            @RequestParam(required = false, defaultValue = "ALL") String processed,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminConsultationResponseDTO> page = queryService.getConsultations(processed, pageable);
        return ResponseEntity.ok(BaseResponse.onSuccess(page));
    }

    @Operation(
            summary = "상담 수락(관리자)",
            description = "해당 상담을 수락 처리합니다. isProcessed=true 로 변경"
    )
    @PutMapping("/{consultationId}/accept")
    public ResponseEntity<BaseResponse<Long>> acceptConsultation(
            @PathVariable Long consultationId
    ) {
        Long id = commandService.accept(consultationId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "상담 메모 저장/수정(관리자)",
            description = "메모를 저장 또는 수정합니다. 빈 문자열이면 메모를 비웁니다.")
    @PutMapping("/{consultationId}/memo")
    public ResponseEntity<BaseResponse<Long>> updateMemo(
            @PathVariable Long consultationId,
            @RequestBody @Valid ConsultationMemoUpdateRequestDTO request
    ) {
        Long id = commandService.updateMemo(consultationId, request.getMemo());
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "상담 슬롯 차단 생성(관리자)",
            description = "특정 날짜/시간 슬롯을 차단합니다. (예약 불가)"
    )
    @PostMapping("/blocks")
    public ResponseEntity<BaseResponse<Long>> createBlock(
            @RequestBody @Valid ConsultationBlockUpsertRequestDTO request
    ) {
        Long id = commandService.createBlock(request.getDate(), request.getTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "상담 슬롯 차단 해제(관리자)",
            description = "특정 날짜/시간 슬롯 차단을 해제합니다. (동일 DTO 사용)"
    )
    @DeleteMapping("/blocks")
    public ResponseEntity<BaseResponse<Long>> deleteBlock(
            @RequestBody @Valid ConsultationBlockUpsertRequestDTO request
    ) {
        Long id = commandService.deleteBlock(request.getDate(), request.getTime());
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }


}
