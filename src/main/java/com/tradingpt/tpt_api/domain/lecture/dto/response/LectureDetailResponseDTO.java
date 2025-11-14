package com.tradingpt.tpt_api.domain.lecture.dto.response;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "강의 상세 응답 DTO")
public class LectureDetailResponseDTO {

    @Schema(description = "강의 ID", example = "15")
    private Long lectureId;

    @Schema(description = "챕터 ID", example = "3")
    private Long chapterId;

    @Schema(description = "강의 제목", example = "Chapter 2. 주린이가 시장에서 살아남는 방법")
    private String title;

    @Schema(description = "강의 본문/설명", example = "이번 강의에서는 투자 심리에 대해 다룹니다.")
    private String content;

    @Schema(description = "동영상 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/lectures/2025-11-07/video.mp4")
    private String videoUrl;

    @Schema(description = "S3 비디오 Key", example = "lectures/2025-11-07/video.mp4")
    private String videoKey;

    @Schema(description = "동영상 전체 길이(초)", example = "1320")
    private Integer durationSeconds;

    @Schema(description = "강의 노출 정책", example = "SUBSCRIBER_WEEKLY")
    private LectureExposure lectureExposure;

    @Schema(description = "챕터 내 정렬 순서(1..N)", example = "1")
    private Integer lectureOrder;

    @Schema(description = "업로더 이름", example = "이승주 트레이너")
    private String trainerName;

    @Schema(description = "수강에 필요한 토큰 수(0=무료)", example = "10")
    private Integer requiredTokens;

    @Schema(description = "누적 시청 시간(초)", example = "600")
    private Integer watchedSeconds;

    @Schema(description = "썸네일 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/2025-11-07/thumbnail.jpg")
    private String thumbnailUrl;

    @Schema(description = "첨부파일 리스트")
    private List<LectureAttachmentResponse> attachments;

    public static LectureDetailResponseDTO from(Lecture lecture) {
        return LectureDetailResponseDTO.builder()
                .lectureId(lecture.getId())
                .chapterId(lecture.getChapter().getId())
                .title(lecture.getTitle())
                .content(lecture.getContent())
                .videoUrl(lecture.getVideoUrl())
                .videoKey(lecture.getVideoKey())
                .durationSeconds(lecture.getDurationSeconds())
                .lectureExposure(lecture.getLectureExposure())
                .lectureOrder(lecture.getLectureOrder())
                .trainerName(lecture.getTrainer().getName())
                .requiredTokens(lecture.getRequiredTokens())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .attachments(
                        lecture.getAttachments() != null
                                ? lecture.getAttachments().stream()
                                .map(LectureAttachmentResponse::from)
                                .collect(Collectors.toList())
                                : List.of()
                )
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "강의 첨부파일 응답 DTO")
    public static class LectureAttachmentResponse {

        @Schema(description = "첨부파일 ID", example = "12")
        private Long attachmentId;

        @Schema(description = "첨부파일 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/2025-11-07/file.pdf")
        private String fileUrl;

        @Schema(description = "첨부파일 Key", example = "uploads/2025-11-07/file.pdf")
        private String fileKey;

        public static LectureAttachmentResponse from(LectureAttachment att) {
            return LectureAttachmentResponse.builder()
                    .attachmentId(att.getId())
                    .fileUrl(att.getFileUrl())
                    .fileKey(att.getFileKey())
                    .build();
        }
    }
}
