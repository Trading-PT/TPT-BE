package com.tradingpt.tpt_api.domain.leveltest.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionUserResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.repository.LevelTestQuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeveltestQueryServiceImpl implements LeveltestQueryService {

	private final LevelTestQuestionRepository questionRepository;

	@Override
	public Slice<LevelTestQuestionUserResponseDTO> getQuestions(Pageable pageable) {
		return questionRepository.findAllBy(pageable)
			.map(LevelTestQuestionUserResponseDTO::from);
	}
}

