package com.tradingpt.tpt_api.domain.event.service.command;

import com.tradingpt.tpt_api.domain.event.dto.request.EventCreateRequestDTO;
import com.tradingpt.tpt_api.domain.event.dto.request.EventUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.event.entity.Event;
import com.tradingpt.tpt_api.domain.event.exception.EventErrorStatus;
import com.tradingpt.tpt_api.domain.event.exception.EventException;
import com.tradingpt.tpt_api.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminEventCommandServiceImpl implements AdminEventCommandService {

    private final EventRepository eventRepository;

    @Override
    public Long createEvent(EventCreateRequestDTO requestDTO) {

        // 이벤트 기간 검증 실패 시
        if (requestDTO.getEndAt().isBefore(requestDTO.getStartAt())) {
            throw new EventException(EventErrorStatus.INVALID_EVENT_PERIOD);
        }

        if (requestDTO.getTokenAmount() <= 0) {
            throw new EventException(EventErrorStatus.INVALID_TOKEN_AMOUNT);
        }

        Event event = Event.builder()
                .name(requestDTO.getName())
                .startAt(requestDTO.getStartAt())
                .endAt(requestDTO.getEndAt())
                .tokenAmount(requestDTO.getTokenAmount())
                .active(true)
                .build();

        Event saved = eventRepository.save(event);
        return saved.getId();
    }

    @Override
    public Long updateEvent(Long eventId, EventUpdateRequestDTO requestDTO) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorStatus.EVENT_NOT_FOUND));

        if (requestDTO.getEndAt().isBefore(requestDTO.getStartAt())) {
            throw new EventException(EventErrorStatus.INVALID_EVENT_PERIOD);
        }

        if (requestDTO.getTokenAmount() <= 0) {
            throw new EventException(EventErrorStatus.INVALID_TOKEN_AMOUNT);
        }

        event.update(
                requestDTO.getName(),
                requestDTO.getStartAt(),
                requestDTO.getEndAt(),
                requestDTO.getTokenAmount(),
                requestDTO.getActive()
        );

        return event.getId();
    }

    @Override
    public Long deleteEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorStatus.EVENT_NOT_FOUND));

        eventRepository.delete(event);

        return eventId;
    }
}
