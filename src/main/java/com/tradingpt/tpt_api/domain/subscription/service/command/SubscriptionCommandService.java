package com.tradingpt.tpt_api.domain.subscription.service.command;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;

/**
 * 구독 명령 서비스 인터페이스
 * 구독 생성, 상태 변경, 결제일 업데이트 등 CUD 작업 정의
 */
public interface SubscriptionCommandService {

    /**
     * 신규 구독 생성 + 즉시 첫 결제 실행
     * PaymentMethod 엔티티를 직접 전달받아 REPEATABLE_READ 트랜잭션 격리 수준 문제를 방지
     *
     * @param customerId 고객 ID
     * @param subscriptionPlanId 구독 플랜 ID
     * @param paymentMethod 결제 수단 엔티티 (REQUIRES_NEW 트랜잭션에서 저장된 경우 ID 조회 불가)
     * @return 생성된 Subscription 엔티티 (첫 결제 완료 후)
     */
    Subscription createSubscriptionWithFirstPayment(
        Long customerId,
        Long subscriptionPlanId,
        PaymentMethod paymentMethod
    );

    /**
     * 다음 결제일 업데이트 (결제 성공 시)
     *
     * @param subscriptionId 구독 ID
     * @param nextBillingDate 다음 결제 예정일
     * @param currentPeriodEnd 현재 결제 주기 종료일
     * @return 업데이트된 Subscription 엔티티
     */
    Subscription updateNextBillingDate(
        Long subscriptionId,
        LocalDate nextBillingDate,
        LocalDate currentPeriodEnd
    );

    /**
     * 결제 실패 횟수 증가
     *
     * @param subscriptionId 구독 ID
     * @return 업데이트된 Subscription 엔티티
     */
    Subscription incrementPaymentFailureCount(Long subscriptionId);

    /**
     * 결제 실패 횟수 리셋 (결제 성공 시)
     *
     * @param subscriptionId 구독 ID
     * @param lastBillingDate 마지막 결제 성공일
     * @return 업데이트된 Subscription 엔티티
     */
    Subscription resetPaymentFailureCount(Long subscriptionId, LocalDate lastBillingDate);

    /**
     * 구독 상태 변경
     *
     * @param subscriptionId 구독 ID
     * @param status 변경할 상태
     * @return 업데이트된 Subscription 엔티티
     */
    Subscription updateSubscriptionStatus(Long subscriptionId, Status status);
}
