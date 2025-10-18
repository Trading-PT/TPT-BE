package com.tradingpt.tpt_api.domain.leveltest.dto.request;

import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 를 사용하려면 Setter가 필요함.
@NoArgsConstructor
@Schema(description = "객관식 문제 등록 요청 DTO")
public class LeveltestMultipleChoiceRequestDTO {

    @Schema(description = "문제 내용", example = "다음 수익률은?")
    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    @Schema(description = "배점", example = "5")
    @NotNull(message = "배점은 필수입니다.")
    private Integer score;

    @Schema(description = "1번 선택지", example = "1퍼")
    @NotBlank(message = "1번 선택지는 필수입니다.")
    private String choice1;

    @Schema(description = "2번 선택지", example = "2퍼")
    @NotBlank(message = "2번 선택지는 필수입니다.")
    private String choice2;

    @Schema(description = "3번 선택지", example = "3퍼")
    private String choice3;

    @Schema(description = "4번 선택지", example = "4퍼")
    private String choice4;

    @Schema(description = "5번 선택지", example = "5퍼")
    private String choice5;

    @Schema(description = "정답 선택 번호(콤마 구분)", example = "1,2")
    @NotBlank(message = "정답 선택 번호는 필수입니다.")
    private String correctChoiceNum;

    @Schema(description = "문제 유형", example = "SHORT_ANSWER")
    @NotNull(message = "문제 유형은 필수입니다.")
    private ProblemType problemType;
}
