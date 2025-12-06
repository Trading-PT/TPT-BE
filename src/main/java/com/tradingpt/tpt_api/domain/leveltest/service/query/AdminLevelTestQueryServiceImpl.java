package com.tradingpt.tpt_api.domain.leveltest.service.query;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptHistoryResponseDTO;
import java.util.ArrayList;
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
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestException;
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
			.orElseThrow(() -> new LevelTestException(LevelTestErrorStatus.QUESTION_NOT_FOUND));

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
				.status(attempt.getStatus())
				.createdAt(attempt.getCreatedAt())
				.build()
		);
	}

	public List<AdminLeveltestAttemptHistoryResponseDTO> getAttemptHistoryByUser(Long userId) {

		// 1) 해당 회원의 전체 시도 리스트(최신 → 오래된 순)
		List<LevelTestAttempt> attempts = leveltestAttemptRepository. findAllByUserIdOrderByCreatedAtAsc(userId);

		List<AdminLeveltestAttemptHistoryResponseDTO> result = new ArrayList<>();

		int order = 1;

		for (LevelTestAttempt attempt : attempts) {

			// 채점 트레이너
			String gradingTrainerName =
					attempt.getTrainer() != null ? attempt.getTrainer().getName() : "-";

			// 담당 트레이너
			String assignedTrainerName =
					attempt.getCustomer().getAssignedTrainer() != null
							? attempt.getCustomer().getAssignedTrainer().getName()
							: "-";

			// DTO.from() 호출
			result.add(
					AdminLeveltestAttemptHistoryResponseDTO.from(
							attempt,
							order++,
							gradingTrainerName,
							assignedTrainerName
					)
			);
		}

		return result;
	}


	@Override
	public AdminLeveltestAttemptDetailResponseDTO getAttemptDetail(Long attemptId) {
		LevelTestAttempt attempt = leveltestAttemptRepository.findById(attemptId)
			.orElseThrow(() -> new LevelTestException(LevelTestErrorStatus.ATTEMPT_NOT_FOUND));

		List<LevelTestResponse> responses = leveltestResponseRepository.findAllByLeveltestAttempt_Id(attemptId);

		return AdminLeveltestAttemptDetailResponseDTO.from(attempt, responses);
	}

}
