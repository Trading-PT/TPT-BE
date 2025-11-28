package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "강의 재생용 Presigned URL 응답 DTO")
public class LecturePlayResponseDTO {

    @Schema(description = "강의 재생용 S3 Presigned GET URL")
    private final String playUrl;

    @Schema(description = "URL 만료까지 남은 시간(초)", example = "7200")
    private final long expiresInSeconds;
}
