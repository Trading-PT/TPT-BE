package com.tradingpt.tpt_api.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.service.command.CustomerCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 피드백 카운트 동기화 스케줄러
 * 매일 새벽 3시에 실행되어 고객의 피드백 카운트를 실제 값으로 동기화
 * 데이터 정합성 보장을 위한 주기적 검증 및 수정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackCountSyncScheduler {

	private final CustomerCommandService customerCommandService;

	/**
	 * 전체 고객의 피드백 카운트 동기화
	 * 매일 새벽 3시(03:00:00)에 실행
	 *
	 * 실행 시간을 새벽 3시로 설정한 이유:
	 * - 사용자 활동이 가장 적은 시간대
	 * - 멤버십 만료 스케줄러(자정)와 시간대 분리
	 * - 정기 결제 스케줄러(새벽 2시)와 시간대 분리
	 */
	@Scheduled(cron = "0 0 3 * * *")
	@SchedulerLock(
		name = "feedbackCountSyncScheduler",
		lockAtMostFor = "PT30M",  // 최대 30분 (대량 데이터 처리 고려)
		lockAtLeastFor = "PT23H"  // 최소 23시간 (하루 1회 보장)
	)
	@Transactional
	public void syncAllFeedbackCounts() {
		log.info("===== 피드백 카운트 동기화 스케줄러 시작 =====");

		try {
			int syncedCount = customerCommandService.syncAllFeedbackCounts();

			log.info("===== 피드백 카운트 동기화 스케줄러 완료: {}명 동기화 =====", syncedCount);

		} catch (Exception e) {
			log.error("===== 피드백 카운트 동기화 스케줄러 실패 =====", e);
			// 예외를 던지지 않고 로그만 남김 (다음 스케줄 실행 보장)
		}
	}
}
