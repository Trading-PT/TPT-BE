package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레벨테스트 제출 결과 응답 DTO (간소화 버전)")
public class LeveltestAttemptSubmitResponseDTO {

    @Schema(description = "생성된 시도(Attempt) ID")
    private Long attemptId;
}
