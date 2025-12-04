package com.tradingpt.tpt_api.domain.subscription.service.command;

import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import java.time.LocalDate;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
import com.tradingpt.tpt_api.domain.subscription.config.PromotionConfig;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionErrorStatus;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionException;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayException;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.subscription.service.RecurringPaymentService;
import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;
import com.tradingpt.tpt_api.domain.subscriptionplan.repository.SubscriptionPlanRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 구독 명령 서비스 구현
 */
@Service
@Transactional
@Slf4j
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final RecurringPaymentService recurringPaymentService;
    private final UserRepository userRepository;

    // 순환 참조 방지를 위한 @Lazy 사용
    public SubscriptionCommandServiceImpl(
        SubscriptionRepository subscriptionRepository,
        CustomerRepository customerRepository,
        SubscriptionPlanRepository subscriptionPlanRepository,
        @Lazy RecurringPaymentService recurringPaymentService,
        UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.recurringPaymentService = recurringPaymentService;
        this.userRepository = userRepository;
    }

    @Override
    public Subscription createSubscriptionWithFirstPayment(
        Long customerId,
        Long subscriptionPlanId,
        PaymentMethod paymentMethod
    ) {
        log.info("신규 구독 생성 시작: customerId={}, planId={}, paymentMethodId={}",
            customerId, subscriptionPlanId, paymentMethod.getId());

        // PaymentMethod null 체크 (REQUIRES_NEW 트랜잭션에서 저장된 엔티티를 직접 전달받음)
        if (paymentMethod == null) {
            throw new PaymentMethodException(PaymentMethodErrorStatus.PAYMENT_METHOD_NOT_FOUND);
        }

        // 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.CUSTOMER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_PLAN_NOT_FOUND));

        // 기존 활성 구독 확인
        subscriptionRepository.findByCustomer_IdAndStatus(customerId, Status.ACTIVE)
            .ifPresent(sub -> {
                throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_ALREADY_EXISTS);
            });

        // 프로모션 대상 여부 확인
        LocalDate today = LocalDate.now();
        boolean isPromotionTarget = PromotionConfig.isWithinPromotionPeriod(today);

        // 프로모션 메모 생성
        String promotionNote = null;
        SubscriptionType subscriptionType = SubscriptionType.REGULAR;

        if (isPromotionTarget) {
            LocalDate promotionEndDate = PromotionConfig.calculatePromotionEndDate(today);
            promotionNote = String.format(
                "프로모션 가입 (2025.12.10-12.17) - %d개월 무료 혜택 종료일: %s",
                PromotionConfig.PROMOTION_FREE_MONTHS,
                promotionEndDate
            );
            subscriptionType = SubscriptionType.PROMOTION;
            log.info("프로모션 대상 고객: 혜택 종료일={}", promotionEndDate);
        }

        // 구독 기간 계산 (프로모션: 2개월, 일반: 1개월)
        int periodMonths = (subscriptionType == SubscriptionType.PROMOTION) ? 2 : 1;
        LocalDate currentPeriodEnd = today.plusMonths(periodMonths).minusDays(1);
        LocalDate nextBillingDate = currentPeriodEnd.plusDays(1);

        // Subscription 엔티티 생성 (초기 상태: PENDING)
        Subscription subscription = Subscription.builder()
            .customer(customer)
            .subscriptionPlan(plan)
            .paymentMethod(paymentMethod)
            .subscribedPrice(plan.getPrice())
            .status(Status.ACTIVE)  // 첫 결제 전이지만 ACTIVE로 설정 (첫 결제 즉시 진행 예정)
            .currentPeriodStart(today)
            .currentPeriodEnd(currentPeriodEnd)
            .nextBillingDate(nextBillingDate)
            .lastBillingDate(null)
            .paymentFailedCount(0)
            .subscriptionType(subscriptionType)
            .promotionNote(promotionNote)
            .build();

        // DB 저장
        subscription = subscriptionRepository.save(subscription);
        log.info("구독 생성 완료: subscriptionId={}", subscription.getId());

        // 즉시 첫 결제 실행
        try {
            recurringPaymentService.executePaymentForSubscription(subscription);
            log.info("신규 구독 첫 결제 성공: subscriptionId={}", subscription.getId());
            customer.setUserStatus(UserStatus.PAID_BEFORE_TRAINER_ASSIGNING);  //결제후, 트레이너 배정 중
        } catch (NicePayException e) {
            log.error("신규 구독 첫 결제 실패 (NicePay 오류): subscriptionId={}, errorCode={}",
                subscription.getId(), e.getErrorStatus().getCode(), e);
            // 첫 결제 실패 시 구독 상태를 PAYMENT_FAILED로 변경
            updateSubscriptionStatus(subscription.getId(), Status.PAYMENT_FAILED);

            // 일시적 오류인 경우 재시도 안내 (HTTP 503)
            if (e.getErrorStatus() == NicePayErrorStatus.TEMPORARY_ERROR) {
                throw new SubscriptionException(SubscriptionErrorStatus.FIRST_PAYMENT_TEMPORARY_FAILED);
            }
            // 영구적 오류인 경우 일반 결제 실패 안내 (HTTP 500)
            throw new SubscriptionException(SubscriptionErrorStatus.FIRST_PAYMENT_FAILED);
        } catch (SubscriptionException e) {
            log.error("신규 구독 첫 결제 실패 (구독 오류): subscriptionId={}", subscription.getId(), e);
            // 구독 예외는 이미 상태가 변경되었으므로 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("신규 구독 첫 결제 실패 (기타 오류): subscriptionId={}", subscription.getId(), e);
            // 첫 결제 실패 시 구독 상태를 PAYMENT_FAILED로 변경
            updateSubscriptionStatus(subscription.getId(), Status.PAYMENT_FAILED);
            throw new SubscriptionException(SubscriptionErrorStatus.FIRST_PAYMENT_FAILED);
        }

        return subscriptionRepository.findById(subscription.getId())
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));
    }

    @Override
    public Subscription updateNextBillingDate(
        Long subscriptionId,
        LocalDate nextBillingDate,
        LocalDate currentPeriodEnd
    ) {
        log.info("다음 결제일 업데이트: subscriptionId={}, nextBillingDate={}", subscriptionId, nextBillingDate);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        // JPA dirty checking을 활용한 업데이트 (save() 호출 불필요)
        subscription.updateBillingDates(nextBillingDate, currentPeriodEnd);

        return subscription;
    }

    @Override
    public Subscription incrementPaymentFailureCount(Long subscriptionId) {
        log.info("결제 실패 횟수 증가: subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        // JPA dirty checking을 활용한 업데이트 (save() 호출 불필요)
        subscription.incrementPaymentFailure();

        return subscription;
    }

    @Override
    public Subscription resetPaymentFailureCount(Long subscriptionId, LocalDate lastBillingDate) {
        log.info("결제 실패 횟수 리셋: subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        // JPA dirty checking을 활용한 업데이트 (save() 호출 불필요)
        subscription.resetPaymentFailure(lastBillingDate);

        return subscription;
    }

    @Override
    public Subscription updateSubscriptionStatus(Long subscriptionId, Status status) {
        log.info("구독 상태 변경: subscriptionId={}, status={}", subscriptionId, status);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        // JPA dirty checking을 활용한 업데이트 (save() 호출 불필요)
        subscription.updateStatus(status);

        return subscription;
    }
}
