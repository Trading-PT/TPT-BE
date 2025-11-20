package com.tradingpt.tpt_api.domain.leveltest.service.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LevelTestQuestionRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLevelTestQueryServiceImpl implements AdminLeveltestQueryService {

	private final LevelTestQuestionRepository leveltestQuestionRepository;
	private final LeveltestAttemptRepository leveltestAttemptRepository;
	private final LeveltestResponseRepository leveltestResponseRepository;

	@Override
	public LevelTestQuestionDetailResponseDTO getQuestion(Long questionId) {
		LevelTestQuestion q = leveltestQuestionRepository.findById(questionId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND));

		return LevelTestQuestionDetailResponseDTO.from(q);
	}

	@Override
	public Slice<LevelTestQuestionDetailResponseDTO> getQuestions(Pageable pageable) {

		return leveltestQuestionRepository.findAllBy(pageable)
			.map(LevelTestQuestionDetailResponseDTO::from);
	}

	@Override
	public Page<AdminLeveltestAttemptListResponseDTO> getAttemptsByStatus(LevelTestStaus status, Pageable pageable) {
		Page<LevelTestAttempt> page = leveltestAttemptRepository.findAllByStatus(status, pageable);

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
		LevelTestAttempt attempt = leveltestAttemptRepository.findById(attemptId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.ATTEMPT_NOT_FOUND));

		List<LevelTestResponse> responses = leveltestResponseRepository.findAllByLeveltestAttempt_Id(attemptId);

		return AdminLeveltestAttemptDetailResponseDTO.from(attempt, responses);
	}

}
