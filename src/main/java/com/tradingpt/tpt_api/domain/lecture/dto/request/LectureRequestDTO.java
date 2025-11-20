package com.tradingpt.tpt_api.domain.lecture.dto.request;

import com.tradingpt.tpt_api.domain.lecture.enums.LectureAttachmentType;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "강의 생성 요청 DTO")
public class LectureRequestDTO {

    @NotNull
    @Schema(description = "어느 챕터에 속할지 ID", example = "1")
    private Long chapterId;

    @NotBlank
    @Schema(description = "강의 제목", example = "Chapter 2. 주린이가 시장에서 살아남는 방법")
    private String title;

    @Schema(description = "강의 본문/설명", example = "이번 강의에서는 ...")
    private String content;

    @Schema(description = "S3에 업로드된 동영상 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/2025-11-07/lecture.mp4")
    private String videoUrl;

    @Schema(description = "S3에 업로드된 동영상 Key", example = "uploads/2025-11-07/lecture.mp4") // ✅ 추가됨
    private String videoKey;

    @Schema(description = "동영상 전체 길이(초)", example = "1320")
    private Integer durationSeconds;

    @NotNull
    @Schema(description = "강의 내 정렬 순서(1..N)", example = "1")
    private Integer lectureOrder;

    @NotNull
    @Schema(description = "공개 범위", example = "SUBSCRIBER_WEEKLY")
    private LectureExposure lectureExposure;

    @Schema(description = "첨부파일들(S3 URL 및 Key 목록)")
    private List<LectureAttachmentDTO> attachments;

    @Schema(description = "강의 수강에 필요한 토큰 수 (0이면 무료)", example = "10")
    private Integer requiredTokens;

    @Schema(description = "썸네일 이미지 URL",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/2025-11-07/thumbnail.jpg")
    private String thumbnailUrl;

    @Getter
    @Schema(description = "강의 첨부파일 DTO")
    public static class LectureAttachmentDTO {

        @NotBlank
        @Schema(description = "첨부파일 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/2025-11-07/file.pdf")
        private String fileUrl;

        @Schema(description = "첨부파일 Key", example = "uploads/2025-11-07/file.pdf")
        private String fileKey;

        @NotNull
        @Schema(description = "첨부파일 타입 (GENERAL, ASSIGNMENT)", example = "GENERAL")
        private LectureAttachmentType attachmentType;
    }
}
