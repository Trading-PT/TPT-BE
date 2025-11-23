package com.tradingpt.tpt_api.domain.event.service.query;

import com.tradingpt.tpt_api.domain.event.dto.response.EventResponseDTO;
import com.tradingpt.tpt_api.domain.event.entity.Event;
import com.tradingpt.tpt_api.domain.event.exception.EventErrorStatus;
import com.tradingpt.tpt_api.domain.event.exception.EventException;
import com.tradingpt.tpt_api.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminEventQueryServiceImpl implements AdminEventQueryService {

    private final EventRepository eventRepository;

    @Override
    public Page<EventResponseDTO> getEvents(Pageable pageable, Boolean onlyActive) {

        Page<Event> page;
        if (Boolean.TRUE.equals(onlyActive)) {
            page = eventRepository.findAllByActiveTrue(pageable);
        } else {
            page = eventRepository.findAll(pageable);
        }

        return page.map(EventResponseDTO::from);
    }

    @Override
    public EventResponseDTO getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorStatus.EVENT_NOT_FOUND));

        return EventResponseDTO.from(event);
    }
}
