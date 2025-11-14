package com.tradingpt.tpt_api.domain.complaint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateComplaintRequestDTO {

    @Schema(description = "민원 제목", example = "8월 결제 환불 요청")
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "민원 내용", example = "8월 결제 환불 요청에 대해 문의드립니다..")
    @NotBlank(message = "본문은 필수입니다.")
    private String content;
}


