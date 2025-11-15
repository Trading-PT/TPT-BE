package com.tradingpt.tpt_api.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.payment.entity.Payment;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 결제 정보 응답 DTO
 */
@Getter
@Builder
@Schema(description = "결제 정보 응답")
public class PaymentResponseDTO {

    @Schema(description = "결제 ID")
    private Long paymentId;

    @Schema(description = "구독 ID")
    private Long subscriptionId;

    @Schema(description = "고객 ID")
    private Long customerId;

    @Schema(description = "주문번호")
    private String orderId;

    @Schema(description = "주문명")
    private String orderName;

    @Schema(description = "결제 금액")
    private BigDecimal amount;

    @Schema(description = "부가세")
    private BigDecimal vat;

    @Schema(description = "할인 금액")
    private BigDecimal discountAmount;

    @Schema(description = "결제 상태")
    private PaymentStatus status;

    @Schema(description = "결제 타입")
    private PaymentType paymentType;

    @Schema(description = "PG 거래번호")
    private String pgTid;

    @Schema(description = "PG 승인번호")
    private String pgAuthCode;

    @Schema(description = "결제 요청 일시")
    private LocalDateTime requestedAt;

    @Schema(description = "결제 완료 일시")
    private LocalDateTime paidAt;

    @Schema(description = "결제 실패 일시")
    private LocalDateTime failedAt;

    @Schema(description = "실패 사유")
    private String failureReason;

    @Schema(description = "청구 기간 시작일")
    private LocalDate billingPeriodStart;

    @Schema(description = "청구 기간 종료일")
    private LocalDate billingPeriodEnd;

    @Schema(description = "프로모션 결제 여부")
    private Boolean isPromotional;

    @Schema(description = "프로모션 상세")
    private String promotionDetail;

    @Schema(description = "영수증 URL")
    private String receiptUrl;

    /**
     * Payment 엔티티를 PaymentResponseDTO로 변환
     */
    public static PaymentResponseDTO from(Payment payment) {
        return PaymentResponseDTO.builder()
            .paymentId(payment.getId())
            .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
            .customerId(payment.getCustomer() != null ? payment.getCustomer().getId() : null)
            .orderId(payment.getOrderId())
            .orderName(payment.getOrderName())
            .amount(payment.getAmount())
            .vat(payment.getVat())
            .discountAmount(payment.getDiscountAmount())
            .status(payment.getStatus())
            .paymentType(payment.getPaymentType())
            .pgTid(payment.getPgTid())
            .pgAuthCode(payment.getPgAuthCode())
            .requestedAt(payment.getRequestedAt())
            .paidAt(payment.getPaidAt())
            .failedAt(payment.getFailedAt())
            .failureReason(payment.getFailureReason())
            .billingPeriodStart(payment.getBillingPeriodStart())
            .billingPeriodEnd(payment.getBillingPeriodEnd())
            .isPromotional(payment.getIsPromotional())
            .promotionDetail(payment.getPromotionDetail())
            .receiptUrl(payment.getReceiptUrl())
            .build();
    }
}
