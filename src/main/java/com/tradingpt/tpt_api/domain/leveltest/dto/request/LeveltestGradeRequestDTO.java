package com.tradingpt.tpt_api.domain.leveltest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LeveltestGradeRequestDTO {

    @Schema(description = "문항별 채점 결과 (문항 ID와 점수)")
    @NotNull
    private List<QuestionGradeDTO> questionGrades;

    @Getter
    @NoArgsConstructor
    public static class QuestionGradeDTO {

        @Schema(description = "질문 ID", example = "101")
        private Long questionId;

        @Schema(description = "응답 ID", example = "101")
        private Long responseId;

        @Schema(description = "부여된 점수", example = "5")
        private Integer score;
    }
}
