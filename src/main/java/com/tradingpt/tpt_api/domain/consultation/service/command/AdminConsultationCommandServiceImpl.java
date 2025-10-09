package com.tradingpt.tpt_api.domain.consultation.service.command;

import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import com.tradingpt.tpt_api.domain.consultation.entity.ConsultationBlock;
import com.tradingpt.tpt_api.domain.consultation.exception.ConsultationErrorStatus;
import com.tradingpt.tpt_api.domain.consultation.exception.ConsultationException;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationBlockRepository;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminConsultationCommandServiceImpl implements AdminConsultationCommandService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationBlockRepository blockRepository;

    @Override
    public Long accept(Long consultationId) {
        Consultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationException(ConsultationErrorStatus.NOT_FOUND));

        c.accept(); // 도메인 메서드로 true 처리
        return c.getId();
    }

    @Override
    public Long updateMemo(Long consultationId, String memo) {
        Consultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationException(ConsultationErrorStatus.NOT_FOUND));

        c.changeMemo(memo);
        return c.getId();
    }

    @Override
    public Long createBlock(LocalDate date, LocalTime time) {
        // 멱등 처리: 이미 있으면 기존 것 반환
        return blockRepository.findByConsultationBlockDateAndConsultationBlockTime(date, time)
                .map(ConsultationBlock::getId)
                .orElseGet(() -> {
                    var block = ConsultationBlock.builder()
                            .consultationBlockDate(date)
                            .consultationBlockTime(time)
                            .build();
                    blockRepository.save(block);
                    return block.getId();
                });
    }

    @Override
    public Long deleteBlock(LocalDate date, LocalTime time) {
        return blockRepository.findByConsultationBlockDateAndConsultationBlockTime(date, time)
                .map(b -> {
                    blockRepository.delete(b);
                    return b.getId();
                })
                .orElse(0L);
    }
}
