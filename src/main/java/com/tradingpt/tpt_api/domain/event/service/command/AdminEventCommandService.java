package com.tradingpt.tpt_api.domain.event.service.command;

import com.tradingpt.tpt_api.domain.event.dto.request.EventCreateRequestDTO;
import com.tradingpt.tpt_api.domain.event.dto.request.EventUpdateRequestDTO;

public interface AdminEventCommandService {

    Long createEvent(EventCreateRequestDTO requestDTO);

    Long updateEvent(Long eventId, EventUpdateRequestDTO requestDTO);

    Long deleteEvent(Long eventId);
}
