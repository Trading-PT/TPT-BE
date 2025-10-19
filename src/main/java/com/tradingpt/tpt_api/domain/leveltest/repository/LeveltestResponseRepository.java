package com.tradingpt.tpt_api.domain.leveltest.repository;

import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeveltestResponseRepository extends JpaRepository<LeveltestResponse, Long> {

    List<LeveltestResponse> findAllByLeveltestAttempt_Id(Long attemptId);

    @Query("select coalesce(sum(r.scoredAwarded), 0) from LeveltestResponse r where r.leveltestAttempt.id = :attemptId")
    Integer sumScoreByAttemptId(@Param("attemptId") Long attemptId);
}
