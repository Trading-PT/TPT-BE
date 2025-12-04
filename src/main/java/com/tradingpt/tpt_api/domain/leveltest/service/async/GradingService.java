package com.tradingpt.tpt_api.domain.leveltest.service.async;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LevelTestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.LeveltestStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradingService {

	private final LeveltestAttemptRepository attemptRepository;
	private final LeveltestResponseRepository responseRepository;

	/**
	 * 여러 서버/스레드에서 동시에 들어와도 "원자적 선점"으로 한쪽만 채점 수행.
	 */
	@Transactional
	public void gradeAttemptSafely(Long attemptId) {

		// 1) 선점: SUBMITTED → GRADING (원자적 상태 플립)
		int acquired = attemptRepository.acquireForGrading(
				attemptId,
				LevelTestStaus.SUBMITTED,
				LevelTestStaus.GRADING
		);

		if (acquired == 0) {
			// 이미 누군가 채점 중이거나 완료됨 → 중복 방지
			return;
		}

		// 2) 채점 대상 로드
		LevelTestAttempt attempt = attemptRepository.findById(attemptId)
				.orElseThrow(() -> new LevelTestException(LevelTestErrorStatus.ATTEMPT_NOT_FOUND));

		// ⭐ 여기서 영속 상태 Customer 를 가져옴
		Customer customer = attempt.getCustomer();

		List<LevelTestResponse> responses =
				responseRepository.findAllByLeveltestAttempt_Id(attemptId);

		int total = 0;
		for (LevelTestResponse r : responses) {
			LevelTestQuestion q = r.getLeveltestQuestion();

			if (q.getProblemType() == ProblemType.MULTIPLE_CHOICE) {
				boolean correct =
						q.getCorrectChoiceNum() != null &&
								q.getCorrectChoiceNum().equals(r.getChoiceNumber());

				int awarded = correct ? q.getScore() : 0;
				r.updateScore(awarded);
				total += awarded;

			} else {
				// 단답/서술형: 채점 전이면 0으로 초기화
				if (r.getScoredAwarded() == null) {
					r.updateScore(0);
				}
			}
		}

		// 총점 반영
		attempt.updateTotalScore(total);

		// (옵션) attempt 상태도 GRADING → COMPLETED 로 바꾸고 싶으면 여기에 메서드 추가
		// attempt.updateStatus(LevelTestStaus.COMPLETED);

		// ⭐ 영속 Customer 상태 변경 → 트랜잭션 종료 시 UPDATE 발생
		customer.setLeveltestStatus(LeveltestStatus.COMPLETED);
	}
}
