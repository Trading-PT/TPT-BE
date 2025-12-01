package com.tradingpt.tpt_api.domain.leveltest.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;

public interface LeveltestAttemptRepository extends JpaRepository<LevelTestAttempt, Long> {
	boolean existsByCustomer_Id(Long customerId);

	@Modifying
	@Query("""
		  update LevelTestAttempt a
		     set a.status = :to
		   where a.id = :attemptId
		     and a.status = :from
		""")
	int acquireForGrading(@Param("attemptId") Long attemptId,
		@Param("from") LevelTestStaus from,
		@Param("to") LevelTestStaus to);

	Page<LevelTestAttempt> findAllByStatus(LevelTestStaus status, Pageable pageable);

	List<LevelTestAttempt> findByCustomer_IdAndStatus(Long customerId, LevelTestStaus status);

	@Query("""
        select a
        from LevelTestAttempt a
            join fetch a.customer c
            left join fetch a.trainer t
        where c.id = :userId
        order by a.createdAt asc
    """)
	List<LevelTestAttempt> findAllByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId);
}
