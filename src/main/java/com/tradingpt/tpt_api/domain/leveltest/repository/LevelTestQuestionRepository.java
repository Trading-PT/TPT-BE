package com.tradingpt.tpt_api.domain.leveltest.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;

public interface LevelTestQuestionRepository extends JpaRepository<LevelTestQuestion, Long> {
	Slice<LevelTestQuestion> findAllBy(Pageable pageable);
}
