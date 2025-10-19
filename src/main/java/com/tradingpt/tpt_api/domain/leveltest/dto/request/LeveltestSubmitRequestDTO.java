package com.tradingpt.tpt_api.domain.leveltest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "레벨테스트 제출 요청 DTO (배치)")
public class LeveltestSubmitRequestDTO {

    @Schema(description = "유저의 문항별 응답 목록")
    @NotEmpty
    private List<QuestionAnswer> answers;

    @Getter
    @Schema(description = "개별 문항 응답 정보")
    public static class QuestionAnswer {
        @NotNull
        @Schema(description = "문항 ID")
        private Long questionId;

        @Schema(description = "객관식일 경우 선택 번호 (예: '1' 또는 '1,3')")
        private String choiceNumber;

        @Schema(description = "단답형/서술형일 경우 작성한 답변 내용")
        private String answerText;
    }
}

