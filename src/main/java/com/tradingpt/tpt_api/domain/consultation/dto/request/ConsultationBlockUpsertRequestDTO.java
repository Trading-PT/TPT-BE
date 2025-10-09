package com.tradingpt.tpt_api.domain.consultation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ConsultationBlockUpsertRequestDTO {

    @Schema(description = "차단 날짜", example = "2025-08-03", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Schema(description = "차단 시간(정시)", example = "16:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;
}
