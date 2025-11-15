package com.tradingpt.tpt_api.domain.subscription.scheduler;

import java.time.Duration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tradingpt.tpt_api.domain.subscription.service.RecurringPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 정기 결제 스케줄러
 * 매일 자정(00:00)에 실행되어 결제 예정일이 도래한 구독에 대해 자동 결제를 수행합니다.
 * ShedLock을 사용하여 분산 환경에서 중복 실행을 방지합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringPaymentScheduler {

    private final RecurringPaymentService recurringPaymentService;

    /**
     * 정기 결제 자동 실행
     *
     * - 실행 시간: 매일 자정 (00:00:00)
     * - ShedLock: 최대 30분 동안 잠금 유지 (다른 인스턴스의 중복 실행 방지)
     * - 최소 실행 간격: 23시간 (같은 인스턴스의 중복 실행 방지)
     */
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
    @SchedulerLock(
        name = "recurringPaymentScheduler",
        lockAtMostFor = "PT30M",  // 최대 30분 동안 락 유지
        lockAtLeastFor = "PT23H"  // 최소 23시간 동안 락 유지 (하루에 한 번만 실행)
    )
    public void executeRecurringPayments() {
        log.info("=== 정기 결제 스케줄러 시작 ===");

        try {
            int processedCount = recurringPaymentService.processRecurringPayments();
            log.info("=== 정기 결제 스케줄러 완료: 처리된 구독 수={} ===", processedCount);
        } catch (Exception e) {
            log.error("=== 정기 결제 스케줄러 실행 중 오류 발생 ===", e);
        }
    }
}
