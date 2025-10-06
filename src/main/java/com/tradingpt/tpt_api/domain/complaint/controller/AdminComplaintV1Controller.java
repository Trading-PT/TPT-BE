package com.tradingpt.tpt_api.domain.complaint.controller;

import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.service.query.AdminComplaintQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/complaint")
@RequiredArgsConstructor
@Tag(name = "관리자 민원", description = "민원 관련 API (관리자용)")
public class AdminComplaintV1Controller {

    private final AdminComplaintQueryService queryService;

    @Operation(summary = "민원 목록(관리자) 조회",
            description = "status: ALL|ANSWERED|UNANSWERED (기본 ALL). 예) ?status=UNANSWERED&page=0&size=20&sort=createdAt,desc")
    @GetMapping
    public Page<AdminComplaintResponseDTO> getAdminComplaints(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return queryService.getComplaints(status, pageable);
    }

    @Operation(
            summary = "민원 단건 조회(관리자)",
            description = "민원 ID로 단건 조회합니다. 예) /api/v1/admin/complaint/1"
    )
    @GetMapping("/{complaintId}")
    public ComplaintResponseDTO getAdminComplaintById(
            @PathVariable("complaintId") Long complaintId
    ) {
        return queryService.getComplaintById(complaintId);
    }
}
