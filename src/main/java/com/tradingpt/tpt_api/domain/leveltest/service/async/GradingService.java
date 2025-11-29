package com.tradingpt.tpt_api.domain.leveltest.service.async;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.LeveltestStatus;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradingService {

	private final LeveltestAttemptRepository attemptRepository;
	private final LeveltestResponseRepository responseRepository;

	/**
	 * 여러 서버/스레드에서 동시에 들어와도 "원자적 선점"으로 한쪽만 채점 수행.
	 */
	@Transactional
	public void gradeAttemptSafely(Long attemptId, Customer customer) {
		// 1) 선점: SUBMITTED → GRADING (원자적 상태 플립)
		int acquired = attemptRepository.acquireForGrading(attemptId, LevelTestStaus.SUBMITTED, LevelTestStaus.GRADING);
		if (acquired == 0) {
			// 이미 누군가 채점 중이거나 완료됨 → 중복 방지
			return;
		}

		// 2) 채점
		LevelTestAttempt attempt = attemptRepository.findById(attemptId)
			.orElseThrow(() -> new LevelTestException(LevelTestErrorStatus.ATTEMPT_NOT_FOUND));

		List<LevelTestResponse> responses = responseRepository.findAllByLeveltestAttempt_Id(attemptId);

		int total = 0;
		for (LevelTestResponse r : responses) {
			LevelTestQuestion q = r.getLeveltestQuestion();
			if (q.getProblemType() == ProblemType.MULTIPLE_CHOICE) {
				boolean correct = q.getCorrectChoiceNum() != null &&
					q.getCorrectChoiceNum().equals(r.getChoiceNumber());
				int awarded = correct ? q.getScore() : 0;
				r.updateScore(awarded);
				total += awarded;
			} else {
				// 단답/서술:  0 유지
				if (r.getScoredAwarded() == null) {
					r.updateScore(0);
				}
			}
		}

		attempt.updateTotalScore(total);
		// JPA 변경감지로 flush
		customer.setLeveltestStatus(LeveltestStatus.COMPLETED);
	}
}
