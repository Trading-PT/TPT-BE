package com.tradingpt.tpt_api.domain.leveltest.service.async;

import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        int acquired = attemptRepository.acquireForGrading(attemptId, LeveltestStaus.SUBMITTED, LeveltestStaus.GRADING);
        if (acquired == 0) {
            // 이미 누군가 채점 중이거나 완료됨 → 중복 방지
            return;
        }

        // 2) 채점
        LeveltestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.ATTEMPT_NOT_FOUND));

        List<LeveltestResponse> responses = responseRepository.findAllByLeveltestAttempt_Id(attemptId);

        int total = 0;
        for (LeveltestResponse r : responses) {
            LeveltestQuestion q = r.getLeveltestQuestion();
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
    }
}
