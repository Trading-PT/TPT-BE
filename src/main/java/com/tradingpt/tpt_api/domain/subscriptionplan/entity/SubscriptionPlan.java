package com.tradingpt.tpt_api.domain.subscriptionplan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "subscription_plan")
public class SubscriptionPlan extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subscription_plan_id")
	private Long id;

	private String name; // 구독 플랜 이름

	private BigDecimal price; // 월 구독료

	private LocalDateTime effectiveFrom; // 시행 시작일

	private LocalDateTime effectiveTo; // 시행 종료일(NULL = 현재 활성)

	private Boolean isActive; // 현재 활성 플랜 여부

}
