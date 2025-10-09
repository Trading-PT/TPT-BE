package com.tradingpt.tpt_api.domain.consultation.service.query;

import com.tradingpt.tpt_api.domain.consultation.dto.response.AdminConsultationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminConsultationQueryService {

    Page<AdminConsultationResponseDTO> getConsultations(String processed, Pageable pageable);
}
