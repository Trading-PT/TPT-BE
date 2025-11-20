package com.tradingpt.tpt_api.domain.leveltest.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionUserResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.repository.LevelTestQuestionRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeveltestQueryServiceImpl implements LeveltestQueryService {

	private final LevelTestQuestionRepository questionRepository;
	private final LeveltestAttemptRepository attemptRepository;
	private final LeveltestResponseRepository responseRepository;

	@Override
	public Slice<LevelTestQuestionUserResponseDTO> getQuestions(Pageable pageable) {
		return questionRepository.findAllBy(pageable)
			.map(LevelTestQuestionUserResponseDTO::from);
	}

	/**
	 * 채점 완료된 시도 리스트 조회
	 */
	@Override
	public List<LeveltestAttemptListResponseDTO> getGradedAttempts(Long customerId) {
		List<LevelTestAttempt> attempts =
			attemptRepository.findByCustomer_IdAndStatus(customerId, LevelTestStaus.GRADED);

		return attempts.stream()
			.map(LeveltestAttemptListResponseDTO::from)
			.toList();
	}

	/**
	 * 특정 시도 상세 조회
	 */
	@Override
	public LeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId) {
		LevelTestAttempt attempt = attemptRepository.findById(attemptId)
			.orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

		List<LevelTestResponse> responses =
			responseRepository.findAllByAttemptIdFetchQuestion(attemptId);

		return LeveltestAttemptDetailResponseDTO.builder()
			.attemptId(attempt.getId())
			.totalScore(attempt.getTotalScore())
			.grade(String.valueOf(attempt.getGrade()))
			.customerId(attempt.getCustomer().getId())
			.responses(
				responses.stream()
					.map(LeveltestAttemptDetailResponseDTO.QuestionResponse::from)
					.toList()
			)
			.build();
	}

}
