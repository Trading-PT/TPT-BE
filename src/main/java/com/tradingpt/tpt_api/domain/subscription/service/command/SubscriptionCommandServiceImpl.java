package com.tradingpt.tpt_api.domain.subscription.service.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.subscription.config.PromotionConfig;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionErrorStatus;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionException;
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
    private final PaymentMethodRepository paymentMethodRepository;
    private final RecurringPaymentService recurringPaymentService;

    // 순환 참조 방지를 위한 @Lazy 사용
    public SubscriptionCommandServiceImpl(
        SubscriptionRepository subscriptionRepository,
        CustomerRepository customerRepository,
        SubscriptionPlanRepository subscriptionPlanRepository,
        PaymentMethodRepository paymentMethodRepository,
        @Lazy RecurringPaymentService recurringPaymentService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.recurringPaymentService = recurringPaymentService;
    }

    @Override
    public Subscription createSubscriptionWithFirstPayment(
        Long customerId,
        Long subscriptionPlanId,
        Long paymentMethodId,
        int baseOpenedLectureCount
    ) {
        log.info("신규 구독 생성 시작: customerId={}, planId={}, paymentMethodId={}",
            customerId, subscriptionPlanId, paymentMethodId);

        // 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_PLAN_NOT_FOUND));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

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

        // Subscription 엔티티 생성 (초기 상태: PENDING)
        Subscription subscription = Subscription.builder()
            .customer(customer)
            .subscriptionPlan(plan)
            .paymentMethod(paymentMethod)
            .subscribedPrice(plan.getPrice())
            .status(Status.ACTIVE)  // 첫 결제 전이지만 ACTIVE로 설정 (첫 결제 즉시 진행 예정)
            .currentPeriodStart(today)
            .currentPeriodEnd(today.plusMonths(1).minusDays(1))
            .nextBillingDate(today.plusMonths(1))
            .lastBillingDate(null)
            .paymentFailedCount(0)
            .subscriptionType(subscriptionType)
            .promotionNote(promotionNote)
            .baseOpenedLectureCount(baseOpenedLectureCount)
            .build();

        // DB 저장
        subscription = subscriptionRepository.save(subscription);
        log.info("구독 생성 완료: subscriptionId={}", subscription.getId());

        // 즉시 첫 결제 실행
        try {
            recurringPaymentService.executePaymentForSubscription(subscription);
            log.info("신규 구독 첫 결제 성공: subscriptionId={}", subscription.getId());
        } catch (Exception e) {
            log.error("신규 구독 첫 결제 실패: subscriptionId={}", subscription.getId(), e);
            // 첫 결제 실패 시 구독 상태를 PAYMENT_FAILED로 변경
            updateSubscriptionStatus(subscription.getId(), Status.PAYMENT_FAILED);
            throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
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

        Subscription updatedSubscription = Subscription.builder()
            .id(subscription.getId())
            .customer(subscription.getCustomer())
            .subscriptionPlan(subscription.getSubscriptionPlan())
            .paymentMethod(subscription.getPaymentMethod())
            .subscribedPrice(subscription.getSubscribedPrice())
            .status(subscription.getStatus())
            .currentPeriodStart(subscription.getCurrentPeriodEnd() != null ?
                subscription.getCurrentPeriodEnd().plusDays(1) : subscription.getCurrentPeriodStart())
            .currentPeriodEnd(currentPeriodEnd)
            .nextBillingDate(nextBillingDate)
            .lastBillingDate(subscription.getLastBillingDate())
            .cancelledAt(subscription.getCancelledAt())
            .cancellationReason(subscription.getCancellationReason())
            .paymentFailedCount(subscription.getPaymentFailedCount())
            .lastPaymentFailedAt(subscription.getLastPaymentFailedAt())
            .subscriptionType(subscription.getSubscriptionType())
            .promotionNote(subscription.getPromotionNote())
            .baseOpenedLectureCount(subscription.getBaseOpenedLectureCount())
            .build();

        return subscriptionRepository.save(updatedSubscription);
    }

    @Override
    public Subscription incrementPaymentFailureCount(Long subscriptionId) {
        log.info("결제 실패 횟수 증가: subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        int newFailureCount = subscription.getPaymentFailedCount() + 1;

        Subscription updatedSubscription = Subscription.builder()
            .id(subscription.getId())
            .customer(subscription.getCustomer())
            .subscriptionPlan(subscription.getSubscriptionPlan())
            .paymentMethod(subscription.getPaymentMethod())
            .subscribedPrice(subscription.getSubscribedPrice())
            .status(subscription.getStatus())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .nextBillingDate(subscription.getNextBillingDate())
            .lastBillingDate(subscription.getLastBillingDate())
            .cancelledAt(subscription.getCancelledAt())
            .cancellationReason(subscription.getCancellationReason())
            .paymentFailedCount(newFailureCount)
            .lastPaymentFailedAt(LocalDateTime.now())
            .subscriptionType(subscription.getSubscriptionType())
            .promotionNote(subscription.getPromotionNote())
            .baseOpenedLectureCount(subscription.getBaseOpenedLectureCount())
            .build();

        return subscriptionRepository.save(updatedSubscription);
    }

    @Override
    public Subscription resetPaymentFailureCount(Long subscriptionId, LocalDate lastBillingDate) {
        log.info("결제 실패 횟수 리셋: subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        Subscription updatedSubscription = Subscription.builder()
            .id(subscription.getId())
            .customer(subscription.getCustomer())
            .subscriptionPlan(subscription.getSubscriptionPlan())
            .paymentMethod(subscription.getPaymentMethod())
            .subscribedPrice(subscription.getSubscribedPrice())
            .status(subscription.getStatus())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .nextBillingDate(subscription.getNextBillingDate())
            .lastBillingDate(lastBillingDate)
            .cancelledAt(subscription.getCancelledAt())
            .cancellationReason(subscription.getCancellationReason())
            .paymentFailedCount(0)
            .lastPaymentFailedAt(null)
            .subscriptionType(subscription.getSubscriptionType())
            .promotionNote(subscription.getPromotionNote())
            .baseOpenedLectureCount(subscription.getBaseOpenedLectureCount())
            .build();

        return subscriptionRepository.save(updatedSubscription);
    }

    @Override
    public Subscription updateSubscriptionStatus(Long subscriptionId, Status status) {
        log.info("구독 상태 변경: subscriptionId={}, status={}", subscriptionId, status);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        Subscription updatedSubscription = Subscription.builder()
            .id(subscription.getId())
            .customer(subscription.getCustomer())
            .subscriptionPlan(subscription.getSubscriptionPlan())
            .paymentMethod(subscription.getPaymentMethod())
            .subscribedPrice(subscription.getSubscribedPrice())
            .status(status)
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .nextBillingDate(subscription.getNextBillingDate())
            .lastBillingDate(subscription.getLastBillingDate())
            .cancelledAt(subscription.getCancelledAt())
            .cancellationReason(subscription.getCancellationReason())
            .paymentFailedCount(subscription.getPaymentFailedCount())
            .lastPaymentFailedAt(subscription.getLastPaymentFailedAt())
            .subscriptionType(subscription.getSubscriptionType())
            .promotionNote(subscription.getPromotionNote())
            .baseOpenedLectureCount(subscription.getBaseOpenedLectureCount())
            .build();

        return subscriptionRepository.save(updatedSubscription);
    }
}
