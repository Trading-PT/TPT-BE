package com.tradingpt.tpt_api.domain.leveltest.entity;

import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestGrade;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "leveltest_attempt")
public class LeveltestAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id", nullable = false)
    private Customer customer;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leveltest_attempt_id")
    private Long id;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "grade")
    private LeveltestGrade grade;

    @Column(name = "status")
    private LeveltestStaus status;

    public void markGraded() {
        this.status = LeveltestStaus.GRADED;
    }

    public void updateTotalScore(int total) {
        this.totalScore = total;
    }
}
