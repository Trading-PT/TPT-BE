package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestQuestionRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLeveltestQueryServiceImpl implements AdminLeveltestQueryService {

    private final LeveltestQuestionRepository leveltestQuestionRepository;
    private final LeveltestAttemptRepository leveltestAttemptRepository;
    private final LeveltestResponseRepository leveltestResponseRepository;

    @Override
    public LeveltestQuestionDetailResponseDTO getQuestion(Long questionId) {
        LeveltestQuestion q = leveltestQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND));

        return LeveltestQuestionDetailResponseDTO.from(q);
    }

    @Override
    public Slice<LeveltestQuestionDetailResponseDTO> getQuestions(Pageable pageable) {

        return leveltestQuestionRepository.findAllBy(pageable)
                .map(LeveltestQuestionDetailResponseDTO::from);
    }

    @Override
    public Page<AdminLeveltestAttemptListResponseDTO> getAttemptsByStatus(LeveltestStaus status, Pageable pageable) {
        Page<LeveltestAttempt> page = leveltestAttemptRepository.findAllByStatus(status, pageable);

        return page.map(attempt ->
                AdminLeveltestAttemptListResponseDTO.builder()
                        .attemptId(attempt.getId())
                        .customerName(attempt.getCustomer().getName())
                        .totalScore(attempt.getTotalScore())
                        .status(attempt.getStatus())
                        .createdAt(attempt.getCreatedAt())
                        .build()
        );
    }

    @Override
    public AdminLeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId) {
        LeveltestAttempt attempt = leveltestAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.ATTEMPT_NOT_FOUND));

        List<LeveltestResponse> responses = leveltestResponseRepository.findAllByLeveltestAttempt_Id(attemptId);

        return AdminLeveltestAttemptDetailResponseDTO.from(attempt, responses);
    }

}
