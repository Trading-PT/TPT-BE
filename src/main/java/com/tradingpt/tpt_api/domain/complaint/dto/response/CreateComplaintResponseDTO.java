package com.tradingpt.tpt_api.domain.complaint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "민원 생성 응답 DTO")
public class CreateComplaintResponseDTO {

    @Schema(description = "민원 ID", example = "1")
    private Long id;
}
