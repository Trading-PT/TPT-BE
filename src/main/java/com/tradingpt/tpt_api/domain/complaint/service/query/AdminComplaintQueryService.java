package com.tradingpt.tpt_api.domain.complaint.service.query;

import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminComplaintQueryService {

    /**
     * 상태값에 따른 민원 목록 조회
     *
     * @param status   ALL | ANSWERED | UNANSWERED
     * @param pageable PageRequest
     * @return 페이지네이션된 민원 목록
     */
    Page<AdminComplaintResponseDTO> getComplaints(String status, Pageable pageable);
    ComplaintResponseDTO getComplaintById(Long complaintId);
}
