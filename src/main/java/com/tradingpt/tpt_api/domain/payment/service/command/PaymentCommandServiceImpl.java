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
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
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
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public Payment createRecurringPayment(
        Long subscriptionId,
        Long customerId,
        Long paymentMethodId,
        BigDecimal amount,
        String orderName,
        String orderId,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        Boolean isPromotional,
        String promotionDetail
    ) {
        log.info("정기 결제 생성: subscriptionId={}, amount={}, orderId={}", subscriptionId, amount, orderId);

        // 엔티티 조회
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_METHOD_NOT_FOUND));

        // Payment 엔티티 생성
        Payment payment = Payment.builder()
            .subscription(subscription)
            .customer(customer)
            .paymentMethod(paymentMethod)
            .orderId(orderId)
            .orderName(orderName)
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

        // Payment 엔티티 업데이트
        Payment updatedPayment = Payment.builder()
            .id(payment.getId())
            .subscription(payment.getSubscription())
            .customer(payment.getCustomer())
            .paymentMethod(payment.getPaymentMethod())
            .orderId(payment.getOrderId())
            .orderName(payment.getOrderName())
            .amount(payment.getAmount())
            .vat(payment.getVat())
            .discountAmount(payment.getDiscountAmount())
            .status(PaymentStatus.SUCCESS)
            .paymentType(payment.getPaymentType())
            .paymentKey(nicePayResponse.getTID())
            .pgTid(nicePayResponse.getTID())
            .pgAuthCode(nicePayResponse.getAuthCode())
            .pgResponseCode(nicePayResponse.getResultCode())
            .pgResponseMessage(nicePayResponse.getResultMsg())
            .requestedAt(payment.getRequestedAt())
            .paidAt(nicePayResponse.getAuthDateAsLocalDateTime())
            .billingPeriodStart(payment.getBillingPeriodStart())
            .billingPeriodEnd(payment.getBillingPeriodEnd())
            .isPromotional(payment.getIsPromotional())
            .promotionDetail(payment.getPromotionDetail())
            .build();

        return paymentRepository.save(updatedPayment);
    }

    @Override
    public Payment markPaymentAsFailed(Long paymentId, String failureCode, String failureReason) {
        log.info("결제 실패 처리: paymentId={}, failureCode={}", paymentId, failureCode);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        // Payment 엔티티 업데이트
        Payment updatedPayment = Payment.builder()
            .id(payment.getId())
            .subscription(payment.getSubscription())
            .customer(payment.getCustomer())
            .paymentMethod(payment.getPaymentMethod())
            .orderId(payment.getOrderId())
            .orderName(payment.getOrderName())
            .amount(payment.getAmount())
            .vat(payment.getVat())
            .discountAmount(payment.getDiscountAmount())
            .status(PaymentStatus.FAILED)
            .paymentType(payment.getPaymentType())
            .paymentKey(payment.getPaymentKey())
            .pgTid(payment.getPgTid())
            .pgAuthCode(payment.getPgAuthCode())
            .pgResponseCode(payment.getPgResponseCode())
            .pgResponseMessage(payment.getPgResponseMessage())
            .requestedAt(payment.getRequestedAt())
            .paidAt(payment.getPaidAt())
            .failedAt(LocalDateTime.now())
            .failureCode(failureCode)
            .failureReason(failureReason)
            .billingPeriodStart(payment.getBillingPeriodStart())
            .billingPeriodEnd(payment.getBillingPeriodEnd())
            .isPromotional(payment.getIsPromotional())
            .promotionDetail(payment.getPromotionDetail())
            .build();

        return paymentRepository.save(updatedPayment);
    }
}
