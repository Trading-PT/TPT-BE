package com.tradingpt.tpt_api.domain.leveltest.repository;

import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeveltestQuestionRepository extends JpaRepository<LeveltestQuestion, Long> {
    Slice<LeveltestQuestion> findAllBy(Pageable pageable);
}
