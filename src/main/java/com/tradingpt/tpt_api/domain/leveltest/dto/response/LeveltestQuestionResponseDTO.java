package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "트레이너에게 배정된 고객 정보 DTO")
public class LeveltestQuestionResponseDTO {

    @Schema(description = "문제 ID")
    private Long questionId;

    @Schema(description = "문제 이미지 URL")
    private String imageUrl;
}

