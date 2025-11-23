package com.tradingpt.tpt_api.domain.event.service;

import com.tradingpt.tpt_api.domain.event.entity.Event;
import com.tradingpt.tpt_api.domain.event.repository.EventRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventTokenServiceImpl implements EventTokenService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void grantSignupTokens(Customer customer) {

        LocalDateTime now = LocalDateTime.now();

        // 2) 현재 진행 중인 이벤트 목록 조회
        List<Event> events = eventRepository
                .findAllByActiveTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqual(now, now);

        if (events.isEmpty()) {
            return; // 진행 중인 이벤트가 없으면 아무 것도 안 함
        }

        // 3) 진행 중인 이벤트들의 토큰 수 합산 (여러 이벤트가 동시에 열려 있을 수도 있으므로)
        int totalTokenAmount = events.stream()
                .mapToInt(Event::getTokenAmount)
                .sum();

        // 4) 고객 토큰 증가
        customer.addToken(totalTokenAmount);
    }
}
