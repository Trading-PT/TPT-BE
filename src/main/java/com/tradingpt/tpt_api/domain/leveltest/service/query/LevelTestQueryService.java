package com.tradingpt.tpt_api.domain.leveltest.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionUserResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptListResponseDTO;

import java.util.List;

public interface LevelTestQueryService {

	// 문제 리스트 조회 (기존)
	Slice<LevelTestQuestionUserResponseDTO> getQuestions(Pageable pageable);

	// 채점 완료된 시도 조회
	List<LeveltestAttemptListResponseDTO> getGradedAttempts(Long customerId);

	// 특정 시도 상세 조회
	LeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId);
}
