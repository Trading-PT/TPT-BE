package com.tradingpt.tpt_api.domain.consultation.repository;

import com.tradingpt.tpt_api.domain.consultation.entity.ConsultationBlock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationBlockRepository extends JpaRepository<ConsultationBlock, Long> {
    List<ConsultationBlock> findAllByConsultationBlockDate(LocalDate date);
    boolean existsByConsultationBlockDateAndConsultationBlockTime(LocalDate date, LocalTime time);
}

