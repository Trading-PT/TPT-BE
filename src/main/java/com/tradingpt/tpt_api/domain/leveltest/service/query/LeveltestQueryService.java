package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionUserResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LeveltestQueryService {

    Slice<LeveltestQuestionUserResponseDTO> getQuestions(Pageable pageable);
}
