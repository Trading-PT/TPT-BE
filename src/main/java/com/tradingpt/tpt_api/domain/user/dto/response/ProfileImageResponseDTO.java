package com.tradingpt.tpt_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "프로파일 이미지 응답 DTO")
public class ProfileImageResponseDTO {

    @Schema(description = "공개 접근 URL")
    private String url;

}
