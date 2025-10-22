package com.tradingpt.tpt_api.domain.leveltest.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionUserResponseDTO;

public interface LeveltestQueryService {

	Slice<LevelTestQuestionUserResponseDTO> getQuestions(Pageable pageable);
}
