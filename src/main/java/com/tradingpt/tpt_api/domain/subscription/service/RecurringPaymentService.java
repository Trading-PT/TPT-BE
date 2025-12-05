package com.tradingpt.tpt_api.domain.subscription.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

		// ✅ 결제 수단 검증 (Payment 생성 전에 확인)
		PaymentMethod paymentMethod = subscription.getPaymentMethod();

		// 현재 구독의 결제수단이 유효하지 않으면 고객의 다른 결제수단 검색
		if (paymentMethod == null || paymentMethod.getIsDeleted() || !paymentMethod.getIsActive()) {
			log.warn("구독의 결제수단이 유효하지 않음. 고객의 다른 결제수단 검색: subscriptionId={}, customerId={}",
				subscription.getId(), subscription.getCustomer().getId());

			// 고객의 유효한 주 결제수단 검색
			paymentMethod = paymentMethodRepository
				.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(subscription.getCustomer())
				.orElse(null);

			if (paymentMethod == null) {
				// ✅ 결제수단 없음 → Payment 생성하지 않고 구독만 EXPIRED 처리
				log.warn("유효한 결제수단 없음 - 구독 만료 처리: subscriptionId={}, customerId={}",
					subscription.getId(), subscription.getCustomer().getId());

				subscriptionCommandService.updateSubscriptionStatus(
					subscription.getId(),
					Status.EXPIRED
				);

				log.info("구독 만료 완료: subscriptionId={} (사유: 결제수단 없음)", subscription.getId());
				return; // Payment 생성하지 않고 정상 종료
			}

			log.info("유효한 결제수단 발견: customerId={}, paymentMethodId={}",
				subscription.getCustomer().getId(), paymentMethod.getId());

			// 구독의 결제수단 업데이트 (다음 결제 시 사용)
			subscription.updatePaymentMethod(paymentMethod);
		}

		// 빌링키 검증
		if (paymentMethod.getBillingKey() == null) {
			log.error("결제수단에 빌링키 없음 - 구독 만료 처리: paymentMethodId={}, subscriptionId={}",
				paymentMethod.getId(), subscription.getId());

			subscriptionCommandService.updateSubscriptionStatus(
				subscription.getId(),
				Status.EXPIRED
			);

			log.info("구독 만료 완료: subscriptionId={} (사유: 빌링키 없음)", subscription.getId());
			return; // Payment 생성하지 않고 정상 종료
		}

		// ✅ 결제 실행일이 프로모션 기간 내인지 확인
		LocalDate today = LocalDate.now();
		boolean isPromotionPeriod = PromotionConfig.isWithinPromotionPeriod(today);

		// 결제 금액 계산 (프로모션 기간 확인)
		BigDecimal paymentAmount = calculatePaymentAmount(activePlan, isPromotionPeriod);

		// 첫 결제 여부 확인
		boolean isFirstPayment = subscription.getLastBillingDate() == null;

		// 청구 기간 계산
		LocalDate billingPeriodStart;
		LocalDate billingPeriodEnd;
		LocalDate nextBillingDate;

		if (isFirstPayment) {
			// 첫 결제: 구독 생성 시 설정된 날짜 사용
			billingPeriodStart = subscription.getCurrentPeriodStart();
			billingPeriodEnd = subscription.getCurrentPeriodEnd();
			nextBillingDate = subscription.getNextBillingDate();
			log.info("첫 결제 처리: subscriptionId={}, 청구기간={} ~ {}, 다음결제일={}",
				subscription.getId(), billingPeriodStart, billingPeriodEnd, nextBillingDate);
		} else {
			// 정기 결제: 결제 실행일 기준으로 계산
			billingPeriodStart = subscription.getCurrentPeriodEnd().plusDays(1);

			// ✅ 프로모션 기간이면 N개월, 아니면 1개월 추가
			int monthsToAdd = isPromotionPeriod ? PromotionConfig.PROMOTION_FREE_MONTHS : 1;
			billingPeriodEnd = today.plusMonths(monthsToAdd).minusDays(1);
			nextBillingDate = today.plusMonths(monthsToAdd);

			log.info("정기 결제 처리: subscriptionId={}, 프로모션기간={}, 추가개월={}, 청구기간={} ~ {}, 다음결제일={}",
				subscription.getId(), isPromotionPeriod, monthsToAdd, billingPeriodStart, billingPeriodEnd, nextBillingDate);
		}

		// 주문번호 생성
		String orderId = NicePayCryptoUtil.generateRecurringMoid(subscription.getId());

		// 주문명 생성 - 한글 (DB 저장용, 이력 조회 시 표시)
		// 예: "기본 구독 플랜 2025년 11월 구독료"
		String orderName = String.format("%s %d년 %d월 구독료",
			activePlan.getName(),
			billingPeriodStart.getYear(),
			billingPeriodStart.getMonthValue());

		// PG 상품명 생성 - 영문 (NicePay 전송용, EUC-KR 인코딩 및 특수문자 문제 회피)
		// 예: "Subscription 11 2025"
		String pgGoodsName = String.format("Subscription %d %d",
			billingPeriodStart.getMonthValue(),
			billingPeriodStart.getYear());

		// 프로모션 여부 확인 (결제 실행일 기준)
		boolean isPromotional = isPromotionPeriod;

		String promotionDetail = null;
		if (isPromotional) {
			promotionDetail = String.format(
				"프로모션 기간 혜택 (%s ~ %s) - %d개월 구독, 결제일: %s, 다음결제일: %s",
				PromotionConfig.PROMOTION_START_DATE,
				PromotionConfig.PROMOTION_END_DATE,
				PromotionConfig.PROMOTION_FREE_MONTHS,
				today,
				nextBillingDate
			);
		}

		// Payment 엔티티 생성
		// Entity를 직접 전달하여 REPEATABLE_READ 트랜잭션 격리 수준 문제 방지
		Payment payment = paymentCommandService.createRecurringPayment(
			subscription,
			subscription.getCustomer(),
			paymentMethod,  // 위에서 검증된 paymentMethod 사용
			paymentAmount,
			orderName,
			pgGoodsName,
			orderId,
			billingPeriodStart,
			billingPeriodEnd,
			isPromotional,
			promotionDetail
		);

		// 결제 실행
		if (paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
			// 0원 결제: 실제 결제 없이 Payment와 Subscription만 업데이트
			handleZeroAmountPayment(subscription, payment, nextBillingDate, billingPeriodEnd, isFirstPayment);
		} else {
			// 일반 결제: 나이스페이 API 호출
			handleRegularPayment(subscription, payment, nextBillingDate, billingPeriodEnd, isFirstPayment);
		}
	}

	/**
	 * 0원 결제 처리 (프로모션 기간)
	 */
	private void handleZeroAmountPayment(
		Subscription subscription,
		Payment payment,
		LocalDate nextBillingDate,
		LocalDate billingPeriodEnd,
		boolean isFirstPayment
	) {
		log.info("0원 결제 처리: subscriptionId={}, paymentId={}, 첫결제={}",
			subscription.getId(), payment.getId(), isFirstPayment);

		// Payment를 SUCCESS로 변경 (실제 PG 호출 없음)
		RecurringPaymentResponseDTO mockResponse = createMockSuccessResponse(payment);
		paymentCommandService.markPaymentAsSuccess(payment.getId(), mockResponse);

		// Subscription 업데이트 (정기 결제인 경우에만 날짜 변경)
		if (!isFirstPayment) {
			subscriptionCommandService.updateNextBillingDate(
				subscription.getId(),
				nextBillingDate,
				billingPeriodEnd
			);
		}

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
		LocalDate billingPeriodEnd,
		boolean isFirstPayment
	) {
		log.info("일반 결제 처리: subscriptionId={}, paymentId={}, amount={}, 첫결제={}",
			subscription.getId(), payment.getId(), payment.getAmount(), isFirstPayment);

		// ✅ 결제 수단은 이미 executePaymentForSubscription()에서 검증됨
		PaymentMethod paymentMethod = subscription.getPaymentMethod();

		try {
			// 나이스페이 결제 실행 (pgGoodsName 사용 - 영문, EUC-KR 인코딩 문제 회피)
			// NicePay API는 금액을 정수 문자열로 요구함 (예: "3500", "50000")
			// BigDecimal.toString()은 "3500.00" 형식이므로 정수로 변환 필요
			String amountString = payment.getAmount().setScale(0, RoundingMode.DOWN).toPlainString();

			RecurringPaymentResponseDTO response = nicePayService.executeRecurringPayment(
				paymentMethod.getBillingKey(),
				amountString,
				payment.getPgGoodsName(),
				payment.getOrderId()
			);

			// 결제 성공 처리
			paymentCommandService.markPaymentAsSuccess(payment.getId(), response);

			// Subscription 업데이트 (정기 결제인 경우에만 날짜 변경)
			if (!isFirstPayment) {
				subscriptionCommandService.updateNextBillingDate(
					subscription.getId(),
					nextBillingDate,
					billingPeriodEnd
				);
			}

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
	 * 결제 금액 계산 (결제 실행일 기준 프로모션 확인)
	 *
	 * 프로모션 적용 기준:
	 * - 결제 실행일이 프로모션 기간 내인 경우 → 프로모션 금액 (0원 또는 설정된 금액)
	 * - 결제 실행일이 프로모션 기간 외인 경우 → 정상 플랜 가격
	 *
	 * @param activePlan 활성 플랜
	 * @param isPromotionPeriod 결제 실행일이 프로모션 기간 내인지 여부
	 * @return 결제 금액
	 */
	private BigDecimal calculatePaymentAmount(SubscriptionPlan activePlan, boolean isPromotionPeriod) {
		if (isPromotionPeriod) {
			log.info("프로모션 기간 내 결제: amount={}", PromotionConfig.PROMOTION_PAYMENT_AMOUNT);
			return PromotionConfig.PROMOTION_PAYMENT_AMOUNT;
		}

		// 프로모션 기간 외: 활성 플랜의 가격 사용
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
		// NicePay 응답 형식에 맞춰 정수 문자열로 설정
		response.setAmt(payment.getAmount().setScale(0, RoundingMode.DOWN).toPlainString());
		response.setAuthCode("000000");
		response.setAuthDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		return response;
	}
}
