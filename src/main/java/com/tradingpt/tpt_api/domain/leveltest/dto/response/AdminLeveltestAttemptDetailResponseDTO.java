package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
@Schema(description = "관리자용 레벨테스트 시도 상세 조회 DTO")
public class AdminLeveltestAttemptDetailResponseDTO {

	@Schema(description = "시도 ID")
	private Long attemptId;

	@Schema(description = "응시자 이름")
	private String customerName;

	@Schema(description = "총점")
	private Integer totalScore;

	@Schema(description = "상태")
	private String status;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "문제별 응답 상세 목록")
	private List<QuestionResponseDetail> questions;

	// 시도 전체 변환 메서드
	public static AdminLeveltestAttemptDetailResponseDTO from(LevelTestAttempt attempt,
		List<LeveltestResponse> responses) {
		List<QuestionResponseDetail> questionDetails = responses.stream()
			.map(QuestionResponseDetail::from)
			.toList();

		return AdminLeveltestAttemptDetailResponseDTO.builder()
			.attemptId(attempt.getId())
			.customerName(attempt.getCustomer().getName())
			.totalScore(attempt.getTotalScore())
			.status(attempt.getStatus().name())
			.createdAt(attempt.getCreatedAt())
			.questions(questionDetails)
			.build();
	}

	@Getter
	@Builder
	@JsonInclude(Include.NON_NULL)
	@Schema(description = "문제별 응답 상세 DTO")
	public static class QuestionResponseDetail {

		private Long responseId;
		private Long questionId;
		private String content;
		private ProblemType problemType;
		private String imageUrl;
		private Integer score;
		private Integer scoredAwarded;

		private MultipleChoicePayload multipleChoice;
		private TextAnswerPayload textAnswer;

		// 개별 문제 변환 메서드
		public static QuestionResponseDetail from(LeveltestResponse r) {
			LevelTestQuestion q = r.getLeveltestQuestion();

			QuestionResponseDetailBuilder builder = QuestionResponseDetail.builder()
				.responseId(r.getId())
				.questionId(q.getId())
				.content(q.getContent())
				.problemType(q.getProblemType())
				.imageUrl(q.getImageUrl())
				.score(q.getScore())
				.scoredAwarded(r.getScoredAwarded());

			if (q.getProblemType() == ProblemType.MULTIPLE_CHOICE) {
				builder.multipleChoice(
					MultipleChoicePayload.builder()
						.choice1(q.getChoice1())
						.choice2(q.getChoice2())
						.choice3(q.getChoice3())
						.choice4(q.getChoice4())
						.choice5(q.getChoice5())
						.choiceNumber(r.getChoiceNumber())
						.correctChoiceNum(q.getCorrectChoiceNum())
						.build()
				);
			} else {
				builder.textAnswer(
					TextAnswerPayload.builder()
						.answerText(r.getAnswerText())
						.build()
				);
			}

			return builder.build();
		}

		@Getter
		@Builder
		@JsonInclude(Include.NON_NULL)
		public static class MultipleChoicePayload {
			private String choice1;
			private String choice2;
			private String choice3;
			private String choice4;
			private String choice5;
			private String choiceNumber;
			private String correctChoiceNum;
		}

		@Getter
		@Builder
		@JsonInclude(Include.NON_NULL)
		public static class TextAnswerPayload {
			private String answerText;
		}
	}
}
