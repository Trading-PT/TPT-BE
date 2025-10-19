package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 제거
@Schema(description = "유저용 레벨테스트 문제 DTO (정답 미포함)")
public class LeveltestQuestionUserResponseDTO {

    private Long questionId;
    private String content;
    private Integer score;
    private ProblemType problemType;
    private String imageUrl;

    private MultipleChoicePayload multipleChoice; // 객관식일 때만 존재
    // textAnswer 필드는 제거 (problemType으로 판단)

    @Getter
    @Builder
    public static class MultipleChoicePayload {
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        private String choice5;
    }

    public static LeveltestQuestionUserResponseDTO from(LeveltestQuestion q) {
        LeveltestQuestionUserResponseDTOBuilder b = LeveltestQuestionUserResponseDTO.builder()
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
                            .build()
            );
        }


        return b.build();
    }
}
