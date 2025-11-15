package com.tradingpt.tpt_api.domain.user.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 멤버십 만료 처리 스케줄러
 * 매일 자정에 실행되어 만료된 PREMIUM 멤버십을 BASIC으로 전환
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipExpirationScheduler {

	private final CustomerRepository customerRepository;

	/**
	 * 만료된 PREMIUM 멤버십을 BASIC으로 전환
	 * 매일 자정(00:00:00)에 실행
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@SchedulerLock(
		name = "membershipExpirationScheduler",
		lockAtMostFor = "PT30M",  // 최대 30분
		lockAtLeastFor = "PT23H"  // 최소 23시간 (하루 1회 보장)
	)
	@Transactional
	public void expireMemberships() {
		log.info("멤버십 만료 처리 시작");

		LocalDateTime now = LocalDateTime.now();

		// 만료된 PREMIUM 멤버십 조회
		List<Customer> expiredCustomers = customerRepository
			.findByMembershipLevelAndMembershipExpiredAtBefore(
				MembershipLevel.PREMIUM,
				now
			);

		if (expiredCustomers.isEmpty()) {
			log.info("만료된 PREMIUM 멤버십 없음");
			return;
		}

		log.info("만료된 PREMIUM 멤버십 고객 수: {}", expiredCustomers.size());

		int successCount = 0;

		for (Customer customer : expiredCustomers) {
			try {
				// BASIC으로 전환
				customer.setMembershipLevel(MembershipLevel.BASIC);
				customer.setMembershipExpiredAt(null);

				log.info("멤버십 만료 처리: customerId={}, 변경: PREMIUM -> BASIC, 만료일: {}",
					customer.getId(), customer.getMembershipExpiredAt());

				successCount++;
			} catch (Exception e) {
				log.error("멤버십 만료 처리 실패: customerId={}", customer.getId(), e);
			}
		}

		customerRepository.saveAll(expiredCustomers);

		log.info("멤버십 만료 처리 완료: 성공={}, 전체={}", successCount, expiredCustomers.size());
	}
}
