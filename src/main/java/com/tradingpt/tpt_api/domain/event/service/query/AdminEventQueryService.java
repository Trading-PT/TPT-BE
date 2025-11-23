package com.tradingpt.tpt_api.domain.event.service.query;

import com.tradingpt.tpt_api.domain.event.dto.response.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminEventQueryService {

    Page<EventResponseDTO> getEvents(Pageable pageable, Boolean onlyActive);

    EventResponseDTO getEvent(Long eventId);
}
