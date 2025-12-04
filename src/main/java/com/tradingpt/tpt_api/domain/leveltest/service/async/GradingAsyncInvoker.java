package com.tradingpt.tpt_api.domain.leveltest.service.async;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingAsyncInvoker {

    private final GradingService gradingService;

    @Async("gradingExecutor") // AsyncConfig에 정의된 쓰레드풀
    public void trigger(Long attemptId, Customer customer) {
        log.info("[GradingAsyncInvoker] 채점 요청 수신 - attemptId={}", attemptId);
        try {
            gradingService.gradeAttemptSafely(attemptId);
            log.info("[GradingAsyncInvoker] 채점 완료 - attemptId={}", attemptId);
        } catch (Exception e) {
            log.error("[GradingAsyncInvoker] 채점 실패 - attemptId={}, message={}", attemptId, e.getMessage(), e);
        }
    }
}
