package com.tradingpt.tpt_api.domain.complaint.controller;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.complaint.dto.request.AdminReplyRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.service.command.AdminComplaintCommandService;
import com.tradingpt.tpt_api.domain.complaint.service.query.AdminComplaintQueryService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/complaints")
@RequiredArgsConstructor
@Tag(name = "관리자 민원", description = "민원 관련 API (관리자용)")
public class AdminComplaintV1Controller {

    private final AdminComplaintQueryService queryService;
    private final AdminComplaintCommandService commandService;

    @Operation(summary = "민원 목록(관리자) 조회",
            description = "status: ALL|ANSWERED|UNANSWERED (기본 ALL). 예) ?status=UNANSWERED&page=0&size=20&sort=createdAt,desc")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminComplaintResponseDTO>>> getAdminComplaints(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminComplaintResponseDTO> page = queryService.getComplaints(status, pageable);
        return ResponseEntity.ok(BaseResponse.onSuccess(page));
    }

    @Operation(summary = "민원 단건 조회(어드민,트레이너 둘다 가능)")
    @GetMapping("/{complaintId}")
    public ResponseEntity<BaseResponse<ComplaintResponseDTO>> getAdminComplaintById(
            @PathVariable("complaintId") Long complaintId
    ) {
        ComplaintResponseDTO dto = queryService.getComplaintById(complaintId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }

    @Operation(summary = "민원 답변 등록(어드민,트레이너 둘다가능)", description = "민원에 대한 최초 답변을 등록합니다.")
    @PostMapping("/{complaintId}/reply")
    public ResponseEntity<BaseResponse<ComplaintResponseDTO>> createReply(
            @PathVariable Long complaintId,
            @RequestBody @Valid AdminReplyRequestDTO request,
            Authentication authentication
    ) {
        Long answeredUserId = ((AuthSessionUser) authentication.getPrincipal()).id();
        ComplaintResponseDTO dto = commandService.upsertReply(complaintId, answeredUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(dto));
    }

    @Operation(summary = "민원 답변 수정(어드민,트레니어 둘다 가능)", description = "기존 답변 내용을 수정합니다.")
    @PutMapping("/{complaintId}/reply")
    public ResponseEntity<BaseResponse<ComplaintResponseDTO>> updateReply(
            @PathVariable Long complaintId,
            @RequestBody @Valid AdminReplyRequestDTO request,
            Authentication authentication
    ) {
        Long answeredUserId = ((AuthSessionUser) authentication.getPrincipal()).id();
        ComplaintResponseDTO dto = commandService.upsertReply(complaintId, answeredUserId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }

    @Operation(summary = "민원 답변 삭제(관리자)", description = "답변을 삭제하고 미답변 상태로 되돌립니다.")
    @DeleteMapping("/{complaintId}/reply")
    public ResponseEntity<BaseResponse<Long>> deleteReply(@PathVariable Long complaintId) {
        commandService.deleteReply(complaintId);
        return ResponseEntity.ok(BaseResponse.onSuccess(complaintId));
    }
}
