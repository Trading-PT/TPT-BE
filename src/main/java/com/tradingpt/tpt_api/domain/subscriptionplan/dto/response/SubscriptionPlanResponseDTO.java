package com.tradingpt.tpt_api.domain.subscriptionplan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 구독 플랜 응답 DTO
 */
@Getter
@Builder
@Schema(description = "구독 플랜 응답 DTO")
public class SubscriptionPlanResponseDTO {

	@Schema(description = "플랜 ID", example = "1")
	private Long id;

	@Schema(description = "플랜 이름", example = "기본 구독 플랜")
	private String name;

	@Schema(description = "월 구독료 (원)", example = "99000")
	private BigDecimal price;

	@Schema(description = "시행 시작일", example = "2025-01-01T00:00:00")
	private LocalDateTime effectiveFrom;

	@Schema(description = "시행 종료일 (null이면 현재 활성)", example = "null")
	private LocalDateTime effectiveTo;

	@Schema(description = "활성 여부", example = "true")
	private Boolean isActive;

	@Schema(description = "생성일시", example = "2025-01-01T00:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시", example = "2025-01-01T00:00:00")
	private LocalDateTime updatedAt;

	/**
	 * SubscriptionPlan 엔티티로부터 DTO 생성
	 *
	 * @param plan 구독 플랜 엔티티
	 * @return DTO
	 */
	public static SubscriptionPlanResponseDTO from(SubscriptionPlan plan) {
		return SubscriptionPlanResponseDTO.builder()
			.id(plan.getId())
			.name(plan.getName())
			.price(plan.getPrice())
			.effectiveFrom(plan.getEffectiveFrom())
			.effectiveTo(plan.getEffectiveTo())
			.isActive(plan.getIsActive())
			.createdAt(plan.getCreatedAt())
			.updatedAt(plan.getUpdatedAt())
			.build();
	}
}
