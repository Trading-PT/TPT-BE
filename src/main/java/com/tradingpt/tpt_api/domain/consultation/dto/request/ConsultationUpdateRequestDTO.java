package com.tradingpt.tpt_api.domain.consultation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ConsultationUpdateRequestDTO {

    @Schema(description = "수정할 상담 ID", example = "15")
    private Long consultationId;

    @Schema(description = "새 상담 날짜", example = "2025-08-10")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate newDate;

    @Schema(description = "새 상담 시간", example = "16:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime newTime;
}
