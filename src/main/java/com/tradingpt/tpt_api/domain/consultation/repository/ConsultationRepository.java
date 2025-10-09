package com.tradingpt.tpt_api.domain.consultation.repository;

import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    @Query("""
    select c.consultationTime as time, count(c) as cnt
    from Consultation c
    where c.consultationDate = :date
    group by c.consultationTime
""")
    List<TimeCount> findCountByDate(@Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c
          from Consultation c
         where c.consultationDate = :date
           and c.consultationTime = :time
        """)
    List<Consultation> findAllForCapacityWithPessimisticLock(@Param("date") LocalDate date,
                                                             @Param("time") LocalTime time);
    //고객별 예약 목록 (최신순)
    List<Consultation> findByCustomerIdOrderByConsultationDateDescConsultationTimeDesc(Long customerId);

    //같은 고객의 중복 예약 방지
    boolean existsByCustomerIdAndConsultationDateAndConsultationTime(Long customerId, LocalDate date, LocalTime time);

    Optional<Consultation> findByIdAndCustomerId(Long id, Long customerId);

    // 상담 진행 유무로 목록 조회(N+1 방지를 위해 유저까지 조회)
    @EntityGraph(attributePaths = "customer")
    Page<Consultation> findByIsProcessed(Boolean isProcessed, Pageable pageable);




    /**
     * Projection interface (JPA가 자동으로 매핑)
     */
    interface TimeCount {
        java.time.LocalTime getTime();
        long getCnt();
    }
}
