package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레벨테스트 문제 상세/목록 공용 DTO (타입별 페이로드 포함)")
public class LeveltestQuestionDetailResponseDTO {

    @Schema(description = "문제 ID")
    @NotNull
    private Long questionId;

    @Schema(description = "문제 내용")
    @NotBlank
    private String content;

    @Schema(description = "배점")
    @NotNull
    private Integer score;

    @Schema(description = "문제 유형")
    @NotNull
    private ProblemType problemType;

    @Schema(description = "문제 이미지 URL")
    @NotBlank
    private String imageUrl;

    /* 타입별 페이로드: 하나만 채워서 리턴 */
    private MultipleChoicePayload multipleChoice; // 객관식일 때
    private TextAnswerPayload textAnswer;         // 단답/서술일 때

    @Getter
    @Builder
    public static class MultipleChoicePayload {
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        private String choice5;
        private String correctChoiceNum;
    }

    @Getter
    @Builder
    public static class TextAnswerPayload {
        private String answerText;
    }

    public static LeveltestQuestionDetailResponseDTO from(LeveltestQuestion q) {
        LeveltestQuestionDetailResponseDTOBuilder b = LeveltestQuestionDetailResponseDTO.builder()
                .questionId(q.getId())
                .content(q.getContent())
                .score(q.getScore())
                .problemType(q.getProblemType())
                .imageUrl(q.getImageUrl());

        if (q.getProblemType() == ProblemType.MULTIPLE_CHOICE) {
            b.multipleChoice(
                    MultipleChoicePayload.builder()
                            .choice1(q.getChoice1())
                            .choice2(q.getChoice2())
                            .choice3(q.getChoice3())
                            .choice4(q.getChoice4())
                            .choice5(q.getChoice5())
                            .correctChoiceNum(q.getCorrectChoiceNum())
                            .build()
            );
        } else if (q.getProblemType() == ProblemType.SHORT_ANSWER
                || q.getProblemType() == ProblemType.SUBJECTIVE) {
            b.textAnswer(
                    TextAnswerPayload.builder()
                            .answerText(q.getAnswerText())
                            .build()
            );
        }

        return b.build();
    }
}
