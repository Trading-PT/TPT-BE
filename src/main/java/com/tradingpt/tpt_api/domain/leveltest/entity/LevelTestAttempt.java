package com.tradingpt.tpt_api.domain.leveltest.entity;

import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestGrade;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.*;
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
	private LeveltestGrade grade;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private LeveltestStaus status;

	public void markGraded() {
		this.status = LeveltestStaus.GRADED;
	}

	public void updateTotalScore(int total) {
		this.totalScore = total;
	}
}
