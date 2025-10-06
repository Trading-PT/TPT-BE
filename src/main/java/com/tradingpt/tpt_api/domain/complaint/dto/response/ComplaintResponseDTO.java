package com.tradingpt.tpt_api.domain.complaint.dto.response;

import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "민원 응답 DTO")
public class ComplaintResponseDTO {

    @Schema(description = "민원 ID", example = "1")
    private Long id;

    @Schema(description = "민원 제목", example = "트레이너 교체 요청드립니다.")
    private String title;

    @Schema(description = "민원 내용", example = "현재 담당 트레이너의 태도 문제로 교체를 요청합니다.")
    private String content;

    @Schema(description = "관리자 답변", example = "고객님 불편을 드려 죄송합니다. 내부적으로 조치하겠습니다.")
    private String complaintReply;

    @Schema(description = "답변 완료 시각", example = "2025-10-02T15:23:45")
    private LocalDateTime answeredAt;

    @Schema(description = "민원 등록 시각", example = "2025-10-01T13:05:00")
    private LocalDateTime createdAt;

    public static ComplaintResponseDTO from(Complaint c) {
        return ComplaintResponseDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .complaintReply(c.getComplaintReply())
                .answeredAt(c.getAnsweredAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
