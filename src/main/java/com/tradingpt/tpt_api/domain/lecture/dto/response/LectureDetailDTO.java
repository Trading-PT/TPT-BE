package com.tradingpt.tpt_api.domain.lecture.dto.response;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "강의 상세 조회 응답 DTO")
public class LectureDetailDTO {

    // ===== 강의 기본 정보 =====
    @Schema(description = "강의 ID", example = "12")
    private Long lectureId;

    @Schema(description = "챕터 ID", example = "3")
    private Long chapterId;

    @Schema(description = "강의 제목", example = "트레이딩 준비하기")
    private String title;

    @Schema(description = "강의 내용/설명", example = "이번 강의에서는...")
    private String content;

    @Schema(description = "비디오 전체 길이(초)", example = "1320")
    private Integer durationSeconds;

    @Schema(description = "강의 순서", example = "1")
    private Integer lectureOrder;

    @Schema(description = "필요 토큰 수(무료 강의일 경우)", example = "10")
    private Integer requiredTokens;

    @Schema(description = "썸네일 이미지 URL", example = "https://bucket.s3.../thumbnail.png")
    private String thumbnailUrl;

    // ===== 첨부파일 정보 =====
    @Schema(description = "강의 첨부파일 목록")
    private List<AttachmentInfo> attachments;

    // ===== 진행도 정보 (없으면 null) =====
    @Schema(description = "누적 시청 시간(초)", example = "120", nullable = true)
    private Integer watchedSeconds;

    @Schema(description = "완강 여부", example = "false", nullable = true)
    private Boolean isCompleted;

    @Schema(description = "마지막 시청 날짜", example = "2025-11-14T12:30:00", nullable = true)
    private LocalDateTime lastWatchedAt;

    @Schema(description = "마지막 재생 위치(초)", example = "45", nullable = true)
    private Integer lastPositionedSeconds;

    /** Lecture + LectureProgress -> DTO */
    public static LectureDetailDTO from(Lecture lecture, LectureProgress progress) {

        List<AttachmentInfo> attachmentInfos = lecture.getAttachments().stream()
                .map(AttachmentInfo::from)
                .collect(Collectors.toList());

        return LectureDetailDTO.builder()
                // --- lecture ---
                .lectureId(lecture.getId())
                .chapterId(lecture.getChapter().getId())
                .title(lecture.getTitle())
                .content(lecture.getContent())
                .durationSeconds(lecture.getDurationSeconds())
                .lectureOrder(lecture.getLectureOrder())
                .requiredTokens(lecture.getRequiredTokens())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .attachments(attachmentInfos)
                // --- progress (null이면 전부 null) ---
                .watchedSeconds(progress != null ? progress.getWatchedSeconds() : null)
                .isCompleted(progress != null ? progress.getIsCompleted() : null)
                .lastWatchedAt(progress != null ? progress.getLastWatchedAt() : null)
                .lastPositionedSeconds(progress != null ? progress.getLastPositionSeconds() : null)
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "강의 첨부파일 정보")
    public static class AttachmentInfo {

        @Schema(description = "첨부파일 ID", example = "101")
        private Long id;

        @Schema(description = "S3 파일 key", example = "lectures/101/file.pdf")
        private String fileKey;

        public static AttachmentInfo from(LectureAttachment attachment) {
            return AttachmentInfo.builder()
                    .id(attachment.getId())
                    .fileKey(attachment.getFileKey())
                    .build();
        }
    }
}
