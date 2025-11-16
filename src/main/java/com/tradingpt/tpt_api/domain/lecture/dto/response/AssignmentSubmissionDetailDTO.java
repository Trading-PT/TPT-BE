package com.tradingpt.tpt_api.domain.lecture.dto.response;

import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "유저 과제 제출 상세 응답 DTO")
public class AssignmentSubmissionDetailDTO {

    @Schema(description = "과제 제출 ID (유저별 과제 엔티티 ID)", example = "10")
    private Long submissionId;

    @Schema(description = "강의 ID", example = "3")
    private Long lectureId;

    @Schema(description = "제출 일시", example = "2025-11-16T21:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "제출한 과제 파일 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/assignments/2025-11-16/hw1.pdf")
    private String fileUrl;

    @Schema(description = "제출한 과제 파일 Key", example = "assignments/2025-11-16/hw1.pdf")
    private String fileKey;

    @Schema(description = "제출 여부", example = "true")
    private boolean submitted;

    public static AssignmentSubmissionDetailDTO from(CustomerAssignment assignment,
                                                     AssignmentAttachment attachment) {
        return AssignmentSubmissionDetailDTO.builder()
                .submissionId(assignment.getId())
                .lectureId(assignment.getLecture().getId())
                .submitted(assignment.isSubmitted())
                .submittedAt(assignment.getSubmittedAt())
                .fileUrl(attachment != null ? attachment.getFileUrl() : null)
                .fileKey(attachment != null ? attachment.getFileKey() : null)
                .build();
    }
}
