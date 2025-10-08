package com.tradingpt.tpt_api.domain.consultation.service.query;

import com.tradingpt.tpt_api.domain.consultation.dto.response.ConsultationResponseDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.response.SlotAvailabilityDTO;
import com.tradingpt.tpt_api.domain.consultation.enums.TimeSlot;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationBlockRepository;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@RequiredArgsConstructor
public class ConsultationQueryServiceImpl implements ConsultationQueryService {

    private static final int MAX_CAPACITY = 2;

    private final ConsultationRepository consultationRepository;
    private final ConsultationBlockRepository blockRepository;

    @Override
    public List<SlotAvailabilityDTO> getDailyAvailability(LocalDate date) {
        // 1. 예약된 시간대 집계 결과 한 번에 가져오기
        Map<LocalTime, Long> reservedCountMap = consultationRepository.findCountByDate(date)
                .stream()
                .collect(Collectors.toMap(
                        ConsultationRepository.TimeCount::getTime,
                        ConsultationRepository.TimeCount::getCnt
                ));

        // 2. 차단된 시간대 한 번에 가져오기
        Set<LocalTime> blockedTimes = blockRepository.findAllByConsultationBlockDate(date)
                .stream()
                .map(block -> block.getConsultationBlockTime())
                .collect(Collectors.toSet());

        // 3. 모든 슬롯에 대해 계산 (DB 접근 X)
        List<SlotAvailabilityDTO> result = new ArrayList<>(TimeSlot.values().length);

        for (TimeSlot slot : TimeSlot.values()) {
            LocalTime time = slot.getTime();
            boolean isBlocked = blockedTimes.contains(time);
            long reserved = reservedCountMap.getOrDefault(time, 0L);
            boolean available = !isBlocked && reserved < MAX_CAPACITY;

            result.add(new SlotAvailabilityDTO(slot, available));
        }

        return result;
    }

    @Override
    public List<ConsultationResponseDTO> getByCustomer(Long customerId) {
        return consultationRepository
                .findByCustomerIdOrderByConsultationDateDescConsultationTimeDesc(customerId)
                .stream()
                .map(ConsultationResponseDTO::from)
                .toList();
    }

}
