package com.tradingpt.tpt_api.domain.consultation.service.query;

import com.tradingpt.tpt_api.domain.consultation.dto.response.ConsultationResponseDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.response.SlotAvailabilityDTO;
import java.time.LocalDate;
import java.util.List;

public interface ConsultationQueryService {
    List<SlotAvailabilityDTO> getDailyAvailability(LocalDate date);
    List<ConsultationResponseDTO> getByCustomer(Long customerId);
}
