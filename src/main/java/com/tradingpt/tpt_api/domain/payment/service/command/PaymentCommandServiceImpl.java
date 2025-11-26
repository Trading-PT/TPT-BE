package com.tradingpt.tpt_api.domain.payment.service.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.payment.entity.Payment;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentType;
import com.tradingpt.tpt_api.domain.payment.exception.PaymentErrorStatus;
import com.tradingpt.tpt_api.domain.payment.exception.PaymentException;
import com.tradingpt.tpt_api.domain.payment.repository.PaymentRepository;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionErrorStatus;
import com.tradingpt.tpt_api.domain.subscription.exception.SubscriptionException;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.RecurringPaymentResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 명령 서비스 구현
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment createRecurringPayment(
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
    ) {
        log.info("정기 결제 생성: subscriptionId={}, amount={}, orderId={}, pgGoodsName={}",
            subscription.getId(), amount, orderId, pgGoodsName);

        // Entity null 체크 (REQUIRES_NEW 트랜잭션에서 저장된 엔티티를 직접 전달받음)
        if (subscription == null) {
            throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND);
        }
        if (customer == null) {
            throw new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND);
        }
        if (paymentMethod == null) {
            throw new PaymentMethodException(PaymentMethodErrorStatus.PAYMENT_METHOD_NOT_FOUND);
        }

        // Payment 엔티티 생성
        Payment payment = Payment.builder()
            .subscription(subscription)
            .customer(customer)
            .paymentMethod(paymentMethod)
            .orderId(orderId)
            .orderName(orderName)
            .pgGoodsName(pgGoodsName)
            .amount(amount)
            .vat(amount.multiply(BigDecimal.valueOf(0.1)))  // 부가세 10%
            .discountAmount(BigDecimal.ZERO)
            .status(PaymentStatus.PENDING)
            .paymentType(PaymentType.RECURRING)
            .requestedAt(LocalDateTime.now())
            .billingPeriodStart(billingPeriodStart)
            .billingPeriodEnd(billingPeriodEnd)
            .isPromotional(isPromotional != null ? isPromotional : Boolean.FALSE)
            .promotionDetail(promotionDetail)
            .build();

        return paymentRepository.save(payment);
    }

    @Override
    public Payment markPaymentAsSuccess(Long paymentId, RecurringPaymentResponseDTO nicePayResponse) {
        log.info("결제 성공 처리: paymentId={}, pgTid={}", paymentId, nicePayResponse.getTID());

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        // JPA dirty checking을 활용한 엔티티 업데이트
        payment.markAsSuccess(
            nicePayResponse.getTID(),               // paymentKey
            nicePayResponse.getTID(),               // pgTid
            nicePayResponse.getAuthCode(),          // authCode
            nicePayResponse.getResultCode(),        // responseCode
            nicePayResponse.getResultMsg(),         // responseMessage
            nicePayResponse.getAuthDateAsLocalDateTime()  // paidAt
        );

        return payment;  // JPA dirty checking이 자동으로 UPDATE 처리
    }

    @Override
    public Payment markPaymentAsFailed(Long paymentId, String failureCode, String failureReason) {
        log.info("결제 실패 처리: paymentId={}, failureCode={}", paymentId, failureCode);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        // JPA dirty checking을 활용한 엔티티 업데이트
        payment.markAsFailed(failureCode, failureReason);

        return payment;  // JPA dirty checking이 자동으로 UPDATE 처리
    }
}
