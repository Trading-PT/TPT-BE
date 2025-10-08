package com.tradingpt.tpt_api.domain.consultation.dto.response;

import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ConsultationResponseDTO {

    @Schema(description = "상담 id", example = "1")
    private Long id;

    @Schema(description = "상담 날짜", example = "2025-08-03")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Schema(description = "상담 시간", example = "14:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

    public static ConsultationResponseDTO from(Consultation consultation) {
        return ConsultationResponseDTO.builder()
                .id(consultation.getId())
                .date(consultation.getConsultationDate())
                .time(consultation.getConsultationTime())
                .build();
    }
}
