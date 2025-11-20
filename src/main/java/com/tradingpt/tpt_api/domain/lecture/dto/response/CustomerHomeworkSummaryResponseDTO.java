package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "특정 회원의 PRO 강의 과제 현황 요약 응답")
public class CustomerHomeworkSummaryResponseDTO {

    @Schema(description = "고객 ID", example = "123")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String customerName;

    @Schema(description = "지금까지 열린 PRO 강의 개수(=주차 수)", example = "3")
    private int totalOpenedCount;

    @Schema(description = "열린 강의 중 미제출 과제 개수", example = "2")
    private int notSubmittedCount;

    @Schema(description = "강의별 과제 현황 목록")
    private List<CustomerHomeworkItemDTO> items;

    // 내부 아이템 DTO
    @Getter
    @Builder
    @Schema(description = "강의별 과제 상태 정보")
    public static class CustomerHomeworkItemDTO {

        @Schema(description = "PRO 강의 ID", example = "45")
        private Long lectureId;

        @Schema(description = "PRO 강의 순번(주차)", example = "1")
        private int order;

        @Schema(description = "강의 제목", example = "1주차: 화면 구성과 기초 개념")
        private String lectureTitle;

        @Schema(
                description = "과제 상태",
                example = "제출",
                allowableValues = {"제출", "미제출", "수강 전"}
        )
        private String status;

        @Schema(description = "제출된 PDF 파일명(없으면 null)", example = "homework_week1.pdf", nullable = true)
        private String submittedFileName;

        @Schema(description = "제출된 PDF 파일 URL(없으면 null)",
                example = "https://bucket.s3.amazonaws.com/assignments/45/homework_week1.pdf",
                nullable = true)
        private String submittedFileUrl;
    }
}
