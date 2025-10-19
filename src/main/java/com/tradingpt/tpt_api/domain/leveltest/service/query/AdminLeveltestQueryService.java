package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionDetailResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface AdminLeveltestQueryService {

    LeveltestQuestionDetailResponseDTO getQuestion(Long questionId);

    Slice<LeveltestQuestionDetailResponseDTO> getQuestions(Pageable pageable);
}
