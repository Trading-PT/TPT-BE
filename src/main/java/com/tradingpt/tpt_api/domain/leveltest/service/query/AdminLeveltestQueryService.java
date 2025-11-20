package com.tradingpt.tpt_api.domain.leveltest.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;

public interface AdminLeveltestQueryService {

	LevelTestQuestionDetailResponseDTO getQuestion(Long questionId);

	Slice<LevelTestQuestionDetailResponseDTO> getQuestions(Pageable pageable);

	Page<AdminLeveltestAttemptListResponseDTO> getAttemptsByStatus(LevelTestStaus status, Pageable pageable);

	AdminLeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId);
}
