package com.tradingpt.tpt_api.domain.consultation.service.command;

import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationCreateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationUpdateRequestDTO;

public interface ConsultationCommandService {
    Long createReservation(Long customerId, ConsultationCreateRequestDTO request);
    Long updateReservation(Long customerId, ConsultationUpdateRequestDTO request);
    void deleteReservation(Long customerId, Long consultationId);
}
