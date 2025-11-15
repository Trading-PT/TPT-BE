package com.tradingpt.tpt_api.domain.subscription.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 구독 정보 응답 DTO
 */
@Getter
@Builder
@Schema(description = "구독 정보 응답")
public class SubscriptionResponseDTO {

    @Schema(description = "구독 ID")
    private Long subscriptionId;

    @Schema(description = "고객 ID")
    private Long customerId;

    @Schema(description = "구독 플랜 ID")
    private Long subscriptionPlanId;

    @Schema(description = "구독 플랜명")
    private String subscriptionPlanName;

    @Schema(description = "결제 수단 ID")
    private Long paymentMethodId;

    @Schema(description = "구독 가격")
    private BigDecimal subscribedPrice;

    @Schema(description = "구독 상태")
    private Status status;

    @Schema(description = "현재 결제 주기 시작일")
    private LocalDate currentPeriodStart;

    @Schema(description = "현재 결제 주기 종료일")
    private LocalDate currentPeriodEnd;

    @Schema(description = "다음 결제 예정일")
    private LocalDate nextBillingDate;

    @Schema(description = "마지막 결제 성공일")
    private LocalDate lastBillingDate;

    @Schema(description = "해지 일시")
    private LocalDateTime cancelledAt;

    @Schema(description = "해지 사유")
    private String cancellationReason;

    @Schema(description = "결제 실패 횟수")
    private Integer paymentFailedCount;

    @Schema(description = "마지막 결제 실패 일시")
    private LocalDateTime lastPaymentFailedAt;

    @Schema(description = "구독 타입")
    private SubscriptionType subscriptionType;

    @Schema(description = "프로모션 메모")
    private String promotionNote;

    @Schema(description = "기준 열린 강의 개수")
    private Integer baseOpenedLectureCount;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    /**
     * Subscription 엔티티를 SubscriptionResponseDTO로 변환
     */
    public static SubscriptionResponseDTO from(Subscription subscription) {
        return SubscriptionResponseDTO.builder()
            .subscriptionId(subscription.getId())
            .customerId(subscription.getCustomer() != null ? subscription.getCustomer().getId() : null)
            .subscriptionPlanId(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().getId() : null)
            .subscriptionPlanName(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().getName() : null)
            .paymentMethodId(subscription.getPaymentMethod() != null ? subscription.getPaymentMethod().getId() : null)
            .subscribedPrice(subscription.getSubscribedPrice())
            .status(subscription.getStatus())
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
            .createdAt(subscription.getCreatedAt())
            .build();
    }
}
