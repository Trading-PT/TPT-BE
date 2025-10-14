package com.tradingpt.tpt_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "트레이너 생성,수정 응답 DTO")
public class TrainerResponseDTO {

    @Schema(description = "트레이너 ID")
    private Long trainerId;

}
