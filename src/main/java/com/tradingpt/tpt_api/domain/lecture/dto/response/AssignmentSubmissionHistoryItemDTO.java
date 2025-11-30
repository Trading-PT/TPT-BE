package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 과제 제출 이력 단건 응답 DTO")
public class AssignmentSubmissionHistoryItemDTO {

    @Schema(description = "과제 엔티티 ID (lecture + user 조합으로 1개)",
            example = "101")
    private Long assignmentId;

    @Schema(description = "강의 ID",
            example = "12")
    private Long lectureId;

    @Schema(description = "제출 차수 (1 = 최초 제출, 2 = 재제출 ...)",
            example = "3")
    private int attemptNo;

    @Schema(description = "과제 파일 S3 key",
            example = "assignments/12/34/2025-11-29-190001-homework.pdf")
    private String fileKey;

    @Schema(description = "과제 파일 다운로드용 CloudFront 서명 URL",
            example = "https://dxxxx.cloudfront.net/assignments/12/34/2025-11-29-190001-homework.pdf?Expires=1732887600&Signature=...")
    private String downloadUrl;

    @Schema(description = "해당 제출이 생성된 시각",
            example = "2025-11-29T19:00:01")
    private LocalDateTime submittedAt;

    public static AssignmentSubmissionHistoryItemDTO of(
            com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment assignment,
            com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment attachment,
            String downloadUrl
    ) {
        return AssignmentSubmissionHistoryItemDTO.builder()
                .assignmentId(assignment.getId())
                .lectureId(assignment.getLecture().getId())
                .attemptNo(attachment.getAttemptNo())
                .fileKey(attachment.getFileKey())
                .downloadUrl(downloadUrl)
                .submittedAt(attachment.getCreatedAt())
                .build();
    }
}
