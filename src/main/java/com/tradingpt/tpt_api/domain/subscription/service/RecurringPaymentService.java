package com.tradingpt.tpt_api.domain.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.payment.entity.Payment;
import com.tradingpt.tpt_api.domain.payment.service.command.PaymentCommandService;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.subscription.config.PromotionConfig;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionErrorStatus;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionException;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.subscription.service.command.SubscriptionCommandService;
import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;
import com.tradingpt.tpt_api.domain.subscriptionplan.repository.SubscriptionPlanRepository;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.service.command.CustomerCommandService;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.RecurringPaymentResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.service.NicePayService;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.util.NicePayCryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정기 결제 비즈니스 로직 서비스
 * 정기 결제 처리, 0원 결제, 프로모션 기간 처리 등 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecurringPaymentService {

	private final SubscriptionRepository subscriptionRepository;
	private final SubscriptionPlanRepository subscriptionPlanRepository;
	private final PaymentMethodRepository paymentMethodRepository;
	private final PaymentCommandService paymentCommandService;
	private final SubscriptionCommandService subscriptionCommandService;
	private final CustomerCommandService customerCommandService;
	private final NicePayService nicePayService;

	/**
	 * 정기 결제 대상 구독 조회 및 결제 처리
	 * 매일 자정에 실행됩니다.
	 *
	 * @return 처리된 구독 수
	 */
	public int processRecurringPayments() {
		log.info("정기 결제 처리 시작");

		LocalDate today = LocalDate.now();
		List<Subscription> dueSubscriptions = subscriptionRepository.findSubscriptionsDueForPayment(today);

		log.info("정기 결제 대상 구독 수: {}", dueSubscriptions.size());

		int successCount = 0;
		int failureCount = 0;

		for (Subscription subscription : dueSubscriptions) {
			try {
				executePaymentForSubscription(subscription);
				successCount++;
			} catch (Exception e) {
				log.error("구독 결제 처리 실패: subscriptionId={}", subscription.getId(), e);
				failureCount++;
			}
		}

		log.info("정기 결제 처리 완료: 성공={}, 실패={}", successCount, failureCount);
		return successCount;
	}

	/**
	 * 단일 구독에 대한 결제 실행
	 *
	 * @param subscription 구독 엔티티
	 */
	public void executePaymentForSubscription(Subscription subscription) {
		log.info("구독 결제 실행: subscriptionId={}", subscription.getId());

		// 활성화된 구독 플랜 조회
		SubscriptionPlan activePlan = subscriptionPlanRepository.findByIsActiveTrue()
			.orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.ACTIVE_SUBSCRIPTION_PLAN_NOT_FOUND));

		// 결제 금액 계산 (프로모션 기간 확인)
		BigDecimal paymentAmount = calculatePaymentAmount(subscription, activePlan);

		// 청구 기간 계산
		LocalDate billingPeriodStart = subscription.getCurrentPeriodEnd() != null ?
			subscription.getCurrentPeriodEnd().plusDays(1) : LocalDate.now();
		LocalDate billingPeriodEnd = billingPeriodStart.plusMonths(1).minusDays(1);

		// 다음 결제일 계산 (1개월 후)
		LocalDate nextBillingDate = billingPeriodEnd.plusDays(1);

		// 주문번호 생성
		String orderId = NicePayCryptoUtil.generateRecurringMoid(subscription.getId());

		// 주문명 생성
		String orderName = String.format("%s %d월 구독료",
			activePlan.getName(),
			billingPeriodStart.getMonthValue());

		// 프로모션 여부 확인
		boolean isPromotional = paymentAmount.compareTo(BigDecimal.ZERO) == 0 ||
			paymentAmount.equals(PromotionConfig.PROMOTION_FIRST_PAYMENT_AMOUNT);

		String promotionDetail = null;
		if (isPromotional && subscription.getSubscriptionType() == SubscriptionType.PROMOTION) {
			LocalDate promotionEndDate = PromotionConfig.calculatePromotionEndDate(
				subscription.getCreatedAt().toLocalDate());
			promotionDetail = String.format(
				"프로모션 가입 혜택 (2025.12.10-12.17) - %d개월 무료, 종료일: %s",
				PromotionConfig.PROMOTION_FREE_MONTHS,
				promotionEndDate
			);
		}

		// Payment 엔티티 생성
		Payment payment = paymentCommandService.createRecurringPayment(
			subscription.getId(),
			subscription.getCustomer().getId(),
			subscription.getPaymentMethod().getId(),
			paymentAmount,
			orderName,
			orderId,
			billingPeriodStart,
			billingPeriodEnd,
			isPromotional,
			promotionDetail
		);

		// 결제 실행
		if (paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
			// 0원 결제: 실제 결제 없이 Payment와 Subscription만 업데이트
			handleZeroAmountPayment(subscription, payment, nextBillingDate, billingPeriodEnd);
		} else {
			// 일반 결제: 나이스페이 API 호출
			handleRegularPayment(subscription, payment, nextBillingDate, billingPeriodEnd);
		}
	}

	/**
	 * 0원 결제 처리 (프로모션 기간)
	 */
	private void handleZeroAmountPayment(
		Subscription subscription,
		Payment payment,
		LocalDate nextBillingDate,
		LocalDate billingPeriodEnd
	) {
		log.info("0원 결제 처리: subscriptionId={}, paymentId={}", subscription.getId(), payment.getId());

		// Payment를 SUCCESS로 변경 (실제 PG 호출 없음)
		RecurringPaymentResponseDTO mockResponse = createMockSuccessResponse(payment);
		paymentCommandService.markPaymentAsSuccess(payment.getId(), mockResponse);

		// Subscription 업데이트
		subscriptionCommandService.updateNextBillingDate(
			subscription.getId(),
			nextBillingDate,
			billingPeriodEnd
		);

		subscriptionCommandService.resetPaymentFailureCount(
			subscription.getId(),
			LocalDate.now()
		);

		// 멤버십 업데이트 (PREMIUM으로 승급, 만료일 설정)
		LocalDateTime membershipExpiredAt = billingPeriodEnd.atTime(23, 59, 59);
		customerCommandService.updateMembershipFromSubscription(
			subscription.getCustomer().getId(),
			MembershipLevel.PREMIUM,
			membershipExpiredAt
		);

		log.info("0원 결제 완료: subscriptionId={}", subscription.getId());
	}

	/**
	 * 일반 결제 처리 (나이스페이 API 호출)
	 */
	private void handleRegularPayment(
		Subscription subscription,
		Payment payment,
		LocalDate nextBillingDate,
		LocalDate billingPeriodEnd
	) {
		log.info("일반 결제 처리: subscriptionId={}, paymentId={}, amount={}",
			subscription.getId(), payment.getId(), payment.getAmount());

		PaymentMethod paymentMethod = subscription.getPaymentMethod();

		// 현재 결제수단 유효성 검증
		if (paymentMethod == null
			|| paymentMethod.getIsDeleted()
			|| !paymentMethod.getIsActive()) {

			log.warn("구독의 결제수단이 유효하지 않음. 고객의 다른 결제수단 검색: subscriptionId={}, customerId={}",
				subscription.getId(), subscription.getCustomer().getId());

			// 고객의 유효한 주 결제수단 검색
			paymentMethod = paymentMethodRepository
				.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(subscription.getCustomer())
				.orElse(null);

			if (paymentMethod == null) {
				log.error("유효한 결제수단 없음: customerId={}, subscriptionId={}",
					subscription.getCustomer().getId(), subscription.getId());
				throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
			}

			log.info("유효한 결제수단 발견: customerId={}, paymentMethodId={}",
				subscription.getCustomer().getId(), paymentMethod.getId());
		}

		if (paymentMethod.getBillingKey() == null) {
			log.error("결제수단에 빌링키 없음: paymentMethodId={}", paymentMethod.getId());
			throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
		}

		try {
			// 나이스페이 결제 실행
			RecurringPaymentResponseDTO response = nicePayService.executeRecurringPayment(
				paymentMethod.getBillingKey(),
				payment.getAmount().toString(),
				payment.getOrderName(),
				payment.getOrderId()
			);

			// 결제 성공 처리
			paymentCommandService.markPaymentAsSuccess(payment.getId(), response);

			// Subscription 업데이트
			subscriptionCommandService.updateNextBillingDate(
				subscription.getId(),
				nextBillingDate,
				billingPeriodEnd
			);

			subscriptionCommandService.resetPaymentFailureCount(
				subscription.getId(),
				LocalDate.now()
			);

			// 멤버십 업데이트 (PREMIUM으로 승급, 만료일 설정)
			LocalDateTime membershipExpiredAt = billingPeriodEnd.atTime(23, 59, 59);
			customerCommandService.updateMembershipFromSubscription(
				subscription.getCustomer().getId(),
				MembershipLevel.PREMIUM,
				membershipExpiredAt
			);

			log.info("결제 성공: subscriptionId={}, amount={}", subscription.getId(), payment.getAmount());

		} catch (Exception e) {
			log.error("결제 실패: subscriptionId={}, paymentId={}", subscription.getId(), payment.getId(), e);

			// 결제 실패 처리
			paymentCommandService.markPaymentAsFailed(
				payment.getId(),
				"PAYMENT_FAILED",
				e.getMessage()
			);

			// 구독 실패 횟수 증가
			Subscription updatedSubscription = subscriptionCommandService.incrementPaymentFailureCount(
				subscription.getId());

			// 실패 횟수가 3회 이상이면 구독 상태를 PAYMENT_FAILED로 변경
			if (updatedSubscription.getPaymentFailedCount() >= PromotionConfig.MAX_PAYMENT_FAILURE_COUNT) {
				subscriptionCommandService.updateSubscriptionStatus(
					subscription.getId(),
					Status.PAYMENT_FAILED
				);
				log.warn("구독 상태 변경: ACTIVE -> PAYMENT_FAILED (subscriptionId={})", subscription.getId());
			}

			throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
		}
	}

	/**
	 * 결제 금액 계산 (프로모션 혜택 확인)
	 *
	 * @param subscription 구독
	 * @param activePlan 활성 플랜
	 * @return 결제 금액
	 */
	private BigDecimal calculatePaymentAmount(Subscription subscription, SubscriptionPlan activePlan) {
		LocalDate today = LocalDate.now();

		// 프로모션 구독인 경우 혜택 종료일 확인
		if (subscription.getSubscriptionType() == SubscriptionType.PROMOTION) {
			// 구독 생성일 기준 프로모션 혜택 종료일 계산
			LocalDate subscriptionCreatedAt = subscription.getCreatedAt().toLocalDate();
			LocalDate promotionEndDate = PromotionConfig.calculatePromotionEndDate(subscriptionCreatedAt);

			log.info("프로모션 구독 결제 금액 계산: subscriptionId={}, 혜택종료일={}, 오늘={}",
				subscription.getId(), promotionEndDate, today);

			// 프로모션 혜택 기간 내인지 확인
			if (today.isBefore(promotionEndDate) || today.isEqual(promotionEndDate)) {
				log.info("프로모션 혜택 기간 내 결제: amount={}", PromotionConfig.PROMOTION_FIRST_PAYMENT_AMOUNT);
				return PromotionConfig.PROMOTION_FIRST_PAYMENT_AMOUNT;
			}

			log.info("프로모션 혜택 종료, 정상 금액 결제: amount={}", activePlan.getPrice());
		}

		// 일반 구독 또는 프로모션 기간 종료: 활성 플랜의 가격 사용
		log.info("정상 금액 결제: amount={}", activePlan.getPrice());
		return activePlan.getPrice();
	}

	/**
	 * 0원 결제용 Mock 응답 생성
	 */
	private RecurringPaymentResponseDTO createMockSuccessResponse(Payment payment) {
		RecurringPaymentResponseDTO response = new RecurringPaymentResponseDTO();
		response.setResultCode("3001");
		response.setResultMsg("프로모션 기간 무료 결제");
		response.setTID("PROMO-" + payment.getOrderId());
		response.setMoid(payment.getOrderId());
		response.setAmt(payment.getAmount().toString());
		response.setAuthCode("000000");
		response.setAuthDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		return response;
	}
}
