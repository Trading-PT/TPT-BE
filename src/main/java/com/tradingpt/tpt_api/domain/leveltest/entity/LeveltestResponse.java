package com.tradingpt.tpt_api.domain.leveltest.entity;

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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "leveltest_response",
	uniqueConstraints = {
		@UniqueConstraint(
			columnNames = {"leveltest_attempt_id", "leveltest_question_id"}
		)
	})
public class LeveltestResponse extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leveltest_question_id", nullable = false)
	private LevelTestQuestion leveltestQuestion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leveltest_attempt_id", nullable = false)
	private LevelTestAttempt leveltestAttempt;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "leveltest_response_id")
	private Long id;

	@Column(name = "answer_text")
	private String answerText; // 주관식/단답형 답변

	@Column(name = "choice_number")
	private String choiceNumber; // 객관식 선택 번호 (ex. "1", "3", "1,2")

	@Column(name = "scored_awarded")
	private Integer scoredAwarded; // 획득 점수 (null = 미채점)

	public void updateScore(int score) {
		this.scoredAwarded = score;
	}
}
