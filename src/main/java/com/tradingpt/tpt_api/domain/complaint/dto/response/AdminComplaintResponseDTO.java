package com.tradingpt.tpt_api.domain.complaint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "관리자 민원 응답 DTO")
public class AdminComplaintResponseDTO {

    @Schema(description = "민원 ID", example = "101")
    private Long complaintId;

    @Schema(description = "성함", example = "김개똥")
    private String userName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "담당 조교", example = "이직원")
    private String trainerName;

    @Schema(description = "민원 제목", example = "트레이너 변경 요청")
    private String title;

    @Schema(description = "답변 여부", example = "true")
    private boolean answered;

    @Schema(description = "답변 작성자(트레이너)", example = "이직원")
    private String answerWriter;

    @Schema(description = "답변 작성시각", example = "2025-06-15T08:00:00")
    private LocalDateTime answeredAt;

    @Schema(description = "민원 등록시각", example = "2025-06-10T09:10:00")
    private LocalDateTime createdAt;
}
