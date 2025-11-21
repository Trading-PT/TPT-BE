package com.tradingpt.tpt_api.domain.user.service.command;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Customer Command Service 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerCommandServiceImpl implements CustomerCommandService {

	private final CustomerRepository customerRepository;
	private final FeedbackRequestRepository feedbackRequestRepository;

	@Override
	public void updateMembershipFromSubscription(
		Long customerId,
		MembershipLevel membershipLevel,
		LocalDateTime expiredAt
	) {
		log.info("멤버십 업데이트: customerId={}, level={}, expiredAt={}",
			customerId, membershipLevel, expiredAt);

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 멤버십 업데이트 (JPA dirty checking으로 자동 반영)
		customer.updateMembership(membershipLevel, expiredAt);

		log.info("멤버십 업데이트 완료: customerId={}, newLevel={}, expiredAt={}",
			customerId, membershipLevel, expiredAt);
	}

	@Override
	public int syncFeedbackCount(Long customerId) {
		log.info("피드백 카운트 동기화 시작: customerId={}", customerId);

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 실제 피드백 요청 개수 조회
		long actualCount = feedbackRequestRepository.countByCustomer_Id(customerId);

		// 불일치 시 경고 로그
		if (customer.getFeedbackRequestCount() != actualCount) {
			log.warn("피드백 카운트 불일치 감지: customerId={}, stored={}, actual={}",
				customerId, customer.getFeedbackRequestCount(), actualCount);

			// 실제 카운트로 동기화 (JPA Dirty Checking)
			customer.syncFeedbackRequestCount((int) actualCount);

			log.info("피드백 카운트 동기화 완료: customerId={}, correctedCount={}",
				customerId, actualCount);
		} else {
			log.debug("피드백 카운트 정합성 확인: customerId={}, count={}", customerId, actualCount);
		}

		return (int) actualCount;
	}

	@Override
	public int syncAllFeedbackCounts() {
		log.info("전체 고객 피드백 카운트 동기화 시작");

		List<Customer> customers = customerRepository.findAll();
		int syncCount = 0;
		int mismatchCount = 0;

		for (Customer customer : customers) {
			long actualCount = feedbackRequestRepository.countByCustomer_Id(customer.getId());

			if (customer.getFeedbackRequestCount() != actualCount) {
				log.warn("피드백 카운트 불일치: customerId={}, stored={}, actual={}",
					customer.getId(), customer.getFeedbackRequestCount(), actualCount);

				customer.syncFeedbackRequestCount((int) actualCount);
				mismatchCount++;
			}
			syncCount++;
		}

		log.info("전체 고객 피드백 카운트 동기화 완료: 총 {}명, 불일치 {}건 수정",
			syncCount, mismatchCount);

		return syncCount;
	}
}
