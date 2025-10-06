package com.tradingpt.tpt_api.domain.complaint.repository;

import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintRepositoryCustom {
    Page<AdminComplaintResponseDTO> findAllWithStatus(String status, Pageable pageable);
}
