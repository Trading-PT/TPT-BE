package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "강의 첨부파일 다운로드 응답 DTO")
public class LectureAttachmentDownloadResponseDTO {

    @Schema(description = "첨부파일 ID", example = "10")
    private Long attachmentId;

    @Schema(description = "파일명", example = "lecture-note-week1.pdf")
    private String fileName;

    @Schema(description = "다운로드 URL (CloudFront Signed URL)", example = "https://dxxx.cloudfront.net/...")
    private String downloadUrl;

    @Schema(description = "URL 만료까지 남은 시간(초)", example = "600")
    private long expiresInSeconds;
}
