package com.tradingpt.tpt_api.domain.leveltest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;

public interface LeveltestResponseRepository extends JpaRepository<LevelTestResponse, Long> {

	List<LevelTestResponse> findAllByLeveltestAttempt_Id(Long attemptId);

	@Query("select coalesce(sum(r.scoredAwarded), 0) from LevelTestResponse r where r.leveltestAttempt.id = :attemptId")
	Integer sumScoreByAttemptId(@Param("attemptId") Long attemptId);

	@Query("""
    select r from LevelTestResponse r
    join fetch r.leveltestQuestion q
    where r.leveltestAttempt.id = :attemptId
    order by r.id
  """)
	List<LevelTestResponse> findAllByAttemptIdFetchQuestion(@Param("attemptId") Long attemptId);
}
