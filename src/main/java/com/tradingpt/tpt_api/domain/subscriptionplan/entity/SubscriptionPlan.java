package com.tradingpt.tpt_api.domain.subscriptionplan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "subscription_plan")
public class SubscriptionPlan extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subscription_plan_id")
	private Long id;

	private String name; // 구독 플랜 이름

	private BigDecimal price; // 월 구독료

	@Builder.Default
	private LocalDateTime effectiveFrom = LocalDateTime.now(); // 시행 시작일

	@Builder.Default
	private LocalDateTime effectiveTo = null; // 시행 종료일(NULL = 현재 활성)

	@Builder.Default
	private Boolean isActive = Boolean.TRUE; // 현재 활성 플랜 여부

	/**
	 * 플랜 활성화
	 * - isActive를 true로 설정
	 * - effectiveTo를 null로 설정 (종료일 제거)
	 */
	public void activate() {
		this.isActive = Boolean.TRUE;
		if (this.effectiveTo != null) {
			this.effectiveTo = null;
		}
	}

	/**
	 * 플랜 비활성화
	 * - isActive를 false로 설정
	 * - effectiveTo를 현재 시각으로 설정 (종료일 기록)
	 */
	public void deactivate() {
		this.isActive = Boolean.FALSE;
		if (this.effectiveTo == null) {
			this.effectiveTo = LocalDateTime.now();
		}
	}

	/**
	 * 활성 여부 확인
	 *
	 * @return 활성 여부
	 */
	public boolean isActive() {
		return isActive != null && isActive;
	}

	/**
	 * 유효 기간 내인지 확인
	 *
	 * @param checkTime 확인 시점 (null이면 현재 시각)
	 * @return 유효 여부
	 */
	public boolean isEffective(LocalDateTime checkTime) {
		if (checkTime == null) {
			checkTime = LocalDateTime.now();
		}

		boolean afterStart = effectiveFrom == null || !checkTime.isBefore(effectiveFrom);
		boolean beforeEnd = effectiveTo == null || checkTime.isBefore(effectiveTo);

		return afterStart && beforeEnd;
	}
}

