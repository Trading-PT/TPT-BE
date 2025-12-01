package com.tradingpt.tpt_api.domain.user.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerDeleteScheduler {

    private final CustomerRepository customerRepository;

    /**
     * 🔥 탈퇴 후 30일 지난 고객을 매일 새벽 3시에 영구 삭제
     * 🔥 EC2 여러대여도 ShedLock으로 오직 1대만 수행
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @SchedulerLock(
            name = "deleteExpiredSoftDeletedCustomers",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT10M"
    )
    @Transactional
    public void deleteExpiredSoftDeletedCustomers() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        // 삭제될 인원 수 로그용
        int beforeCount = customerRepository.countAllByDeletedAtBefore(threshold);
        if (beforeCount == 0) {
            log.info("[CustomerDeletionScheduler] 삭제할 탈퇴 30일 경과 회원 없음.");
            return;
        }

        customerRepository.deleteAllByDeletedAtBefore(threshold);

        log.info("[CustomerDeletionScheduler] {}명의 탈퇴 후 30일 경과 회원 영구 삭제 완료", beforeCount);
    }
}
