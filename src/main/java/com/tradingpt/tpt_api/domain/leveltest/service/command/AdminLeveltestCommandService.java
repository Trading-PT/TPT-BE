package com.tradingpt.tpt_api.domain.leveltest.service.command;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestGradeRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestMultipleChoiceRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubjectiveRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AdminLeveltestCommandService {

    LeveltestQuestionResponseDTO createMultipleChoiceQuestion(LeveltestMultipleChoiceRequestDTO req, MultipartFile image);
    LeveltestQuestionResponseDTO createTextAnswerQuestion(LeveltestSubjectiveRequestDTO req, MultipartFile image);

    // ===== 수정 =====
    LeveltestQuestionResponseDTO updateMultipleChoiceQuestion(Long questionId, LeveltestMultipleChoiceRequestDTO req, MultipartFile image);
    LeveltestQuestionResponseDTO updateTextAnswerQuestion(Long questionId, LeveltestSubjectiveRequestDTO req, MultipartFile image);

    // ===== 삭제 =====
    LeveltestQuestionResponseDTO deleteQuestion(Long questionId);

    void applyManualGrading(Long attemptId, LeveltestGradeRequestDTO request);

}
