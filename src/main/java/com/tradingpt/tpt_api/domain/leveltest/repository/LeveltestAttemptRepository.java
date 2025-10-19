package com.tradingpt.tpt_api.domain.leveltest.repository;

import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeveltestAttemptRepository extends JpaRepository<LeveltestAttempt, Long> {
    boolean existsByCustomer_Id(Long customerId);
    @Modifying
    @Query("""
      update LeveltestAttempt a
         set a.status = :to
       where a.id = :attemptId
         and a.status = :from
    """)
    int acquireForGrading(@Param("attemptId") Long attemptId,
                          @Param("from") LeveltestStaus from,
                          @Param("to") LeveltestStaus to);

    Page<LeveltestAttempt> findAllByStatus(LeveltestStaus status, Pageable pageable);
}
