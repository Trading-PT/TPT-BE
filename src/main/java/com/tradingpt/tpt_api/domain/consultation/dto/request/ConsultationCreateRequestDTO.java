package com.tradingpt.tpt_api.domain.consultation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ConsultationCreateRequestDTO {

    @Schema(description = "상담 날짜", example = "2025-08-03")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Schema(description = "상담 시간", example = "14:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;
}
