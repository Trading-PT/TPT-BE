package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionUserResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeveltestQueryServiceImpl implements LeveltestQueryService {

    private final LeveltestQuestionRepository questionRepository;

    @Override
    public Slice<LeveltestQuestionUserResponseDTO> getQuestions(Pageable pageable) {
        return questionRepository.findAllBy(pageable)
                .map(LeveltestQuestionUserResponseDTO::from);
    }
}

