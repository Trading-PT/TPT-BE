package com.tradingpt.tpt_api.domain.leveltest.dto.request;

import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "서술형 문제 등록 요청 DTO")
public class LeveltestSubjectiveRequestDTO {

    @Schema(description = "문제 내용", example = "HTTP 프로토콜의 특징과 동작 방식을 서술하시오.")
    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    @Schema(description = "배점", example = "10")
    @NotNull(message = "배점은 필수입니다.")
    private Integer score;

    @Schema(description = "정답 내용", example = "HTTP는 비연결성, 무상태성을 가지며 요청-응답 구조로 동작한다.")
    @NotBlank(message = "정답 내용은 필수입니다.")
    private String answerText;

    @Schema(description = "문제 유형", example = "SUBJECTIVE")
    @NotNull(message = "문제 유형은 필수입니다.")
    private ProblemType problemType;
}
