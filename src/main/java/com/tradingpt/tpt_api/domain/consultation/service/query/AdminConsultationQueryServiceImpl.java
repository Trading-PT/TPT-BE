package com.tradingpt.tpt_api.domain.consultation.service.query;

import com.tradingpt.tpt_api.domain.consultation.dto.response.AdminConsultationResponseDTO;
import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class AdminConsultationQueryServiceImpl implements AdminConsultationQueryService {

    private final ConsultationRepository consultationRepository;

    @Override
    public Page<AdminConsultationResponseDTO> getConsultations(String processed, Pageable pageable) {
        Boolean processedFilter = parseProcessed(processed);

        Page<Consultation> page = (processedFilter == null)
                ? consultationRepository.findAll(pageable)
                : consultationRepository.findByIsProcessed(processedFilter, pageable);

        return page.map(AdminConsultationResponseDTO::from);
    }

    /** "ALL" | "TRUE" | "FALSE" → null | true | false */
    private Boolean parseProcessed(String processed) {
        if (processed == null) return null;
        String v = processed.trim().toUpperCase();
        switch (v) {
            case "ALL":   return null;
            case "TRUE":  return Boolean.TRUE;
            case "FALSE": return Boolean.FALSE;
            default:      return null; // 알 수 없는 값이면 ALL 처리(원하면 예외로 바꿔도 됨)
        }
    }
}
