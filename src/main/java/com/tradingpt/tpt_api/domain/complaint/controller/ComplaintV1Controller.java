package com.tradingpt.tpt_api.domain.complaint.controller;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.complaint.dto.request.CreateComplaintRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.CreateComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.service.command.ComplaintCommandService;
import com.tradingpt.tpt_api.domain.complaint.service.query.ComplaintQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaint")
@RequiredArgsConstructor
@Tag(name = "고객 민원", description = "민원 관련 API (고객용)")
public class ComplaintV1Controller {

    private final ComplaintCommandService complaintCommandService;
    private final ComplaintQueryService complaintQueryService;

    @Operation(summary = "민원 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<CreateComplaintResponseDTO>> createComplaint(
            Authentication auth,
            @Valid @RequestBody CreateComplaintRequestDTO request
    ) {
        Long userId = ((AuthSessionUser) auth.getPrincipal()).id();
        CreateComplaintResponseDTO dto = complaintCommandService.createComplaint(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.onSuccessCreate(dto));
    }

    @Operation(summary = "내 민원 목록 조회", description = "유저 ID로 자신의 민원 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ComplaintResponseDTO>>> getUserComplaints(Authentication auth) {
        Long userId = ((AuthSessionUser) auth.getPrincipal()).id();
        List<ComplaintResponseDTO> list = complaintQueryService.getComplaintsByUser(userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(list));
    }
}
