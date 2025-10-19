package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface AdminLeveltestQueryService {

    LeveltestQuestionDetailResponseDTO getQuestion(Long questionId);

    Slice<LeveltestQuestionDetailResponseDTO> getQuestions(Pageable pageable);

    Page<AdminLeveltestAttemptListResponseDTO> getAttemptsByStatus(LeveltestStaus status, Pageable pageable);

    AdminLeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId);
}
