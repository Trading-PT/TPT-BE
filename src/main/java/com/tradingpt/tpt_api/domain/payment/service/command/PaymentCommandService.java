package com.tradingpt.tpt_api.domain.payment.service.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.payment.entity.Payment;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.RecurringPaymentResponseDTO;

/**
 * 결제 명령 서비스 인터페이스
 * 결제 생성, 상태 변경 등 CUD 작업 정의
 */
public interface PaymentCommandService {

    /**
     * 정기 결제 생성 (PENDING 상태)
     * Entity를 직접 전달받아 REPEATABLE_READ 트랜잭션 격리 수준 문제를 방지
     *
     * @param subscription 구독 엔티티 (REQUIRES_NEW 트랜잭션에서 저장된 경우 ID 조회 불가)
     * @param customer 고객 엔티티
     * @param paymentMethod 결제 수단 엔티티
     * @param amount 결제 금액
     * @param orderName 주문명 - 한글 (이력 조회용, 예: 기본 구독 플랜 2025년 11월 구독료)
     * @param pgGoodsName PG 상품명 - 영문 (NicePay 전송용, 예: Subscription 11/2025)
     * @param orderId 주문번호
     * @param billingPeriodStart 청구 기간 시작일
     * @param billingPeriodEnd 청구 기간 종료일
     * @param isPromotional 프로모션 결제 여부
     * @param promotionDetail 프로모션 상세
     * @return 생성된 Payment 엔티티
     */
    Payment createRecurringPayment(
        Subscription subscription,
        Customer customer,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String orderName,
        String pgGoodsName,
        String orderId,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        Boolean isPromotional,
        String promotionDetail
    );

    /**
     * 결제 성공 처리
     *
     * @param paymentId 결제 ID
     * @param nicePayResponse 나이스페이 결제 응답
     * @return 업데이트된 Payment 엔티티
     */
    Payment markPaymentAsSuccess(Long paymentId, RecurringPaymentResponseDTO nicePayResponse);

    /**
     * 결제 실패 처리
     *
     * @param paymentId 결제 ID
     * @param failureCode 실패 코드
     * @param failureReason 실패 사유
     * @return 업데이트된 Payment 엔티티
     */
    Payment markPaymentAsFailed(Long paymentId, String failureCode, String failureReason);
}
