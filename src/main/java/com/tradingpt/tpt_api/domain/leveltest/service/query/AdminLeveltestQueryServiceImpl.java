package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestQuestionErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestQuestionException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLeveltestQueryServiceImpl implements AdminLeveltestQueryService {

    private final LeveltestQuestionRepository leveltestQuestionRepository;

    @Override
    public LeveltestQuestionDetailResponseDTO getQuestion(Long questionId) {
        LeveltestQuestion q = leveltestQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LeveltestQuestionException(LeveltestQuestionErrorStatus.QUESTION_NOT_FOUND));

        return LeveltestQuestionDetailResponseDTO.from(q);
    }

    @Override
    public Slice<LeveltestQuestionDetailResponseDTO> getQuestions(Pageable pageable) {

        return leveltestQuestionRepository.findAllBy(pageable)
                .map(LeveltestQuestionDetailResponseDTO::from);
    }
}
