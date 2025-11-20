package com.tradingpt.tpt_api.domain.leveltest.entity;

import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestGrade;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "level_test_attempt")
public class LevelTestAttempt extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "leveltest_attempt_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", referencedColumnName = "user_id", nullable = false)
	private Customer customer;

	@Column(name = "total_score")
	private Integer totalScore;

	@Enumerated(EnumType.STRING)
	@Column(name = "grade")
	private LevelTestGrade grade;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private LevelTestStaus status;

	public void markGraded() {
		this.status = LevelTestStaus.GRADED;
	}

	public void updateTotalScore(int total) {
		this.totalScore = total;
	}
}
