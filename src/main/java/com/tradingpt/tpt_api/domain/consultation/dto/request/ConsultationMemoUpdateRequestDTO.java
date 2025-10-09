package com.tradingpt.tpt_api.domain.consultation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ConsultationMemoUpdateRequestDTO {

    @Schema(description = "상담 메모", example = "상담 시간 변경 요청, 고객 노쇼 가능성 있음")
    private String memo; // null 또는 ""이면 비우기로 처리
}
