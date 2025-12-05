package com.tradingpt.tpt_api.domain.lecture.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseStatusScheduler {

	private final CustomerRepository customerRepository;

	// 매달 1일 00:10 (다른 스케줄러와 시간 분산)
	@Scheduled(cron = "0 10 0 1 * *")
	@SchedulerLock(
		name = "courseStatusMonthlyJob",
		lockAtLeastFor = "PT1M",
		lockAtMostFor = "PT10M"
	)
	/**
	 * 매달 1일 새벽 1시에 PENDING_COMPLETION 상태인 고객을 AFTER_COMPLETION으로 전환
	 * 완강 시점(completedAt)도 함께 기록하여 평가 대상 기간 판별에 활용
	 */
	@Transactional
	public void updateCompletedUsers() {
		List<Customer> pendingUsers =
			customerRepository.findAllByCourseStatus(CourseStatus.PENDING_COMPLETION);

		LocalDateTime completedAt = LocalDateTime.now();  // 완강 시점 기록
		for (Customer user : pendingUsers) {
			user.completeTraining(completedAt);  // ✅ completedAt도 함께 설정
		}
	}
}

