package com.tradingpt.tpt_api.domain.consultation.dto.response;

import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class AdminConsultationResponseDTO {

    @Schema(description = "상담 ID", example = "1")
    private Long id;

    @Schema(description = "신청자 이름", example = "김개똥")
    private String customerName;

    @Schema(description = "신청자 전화번호", example = "010-1234-5678")
    private String customerPhoneNumber;

    @Schema(description = "상담 날짜", example = "2025-08-03")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Schema(description = "상담 시간", example = "14:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

    @Schema(description = "상담 신청 시각", example = "2025-06-15T13:50:21")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;

    @Schema(description = "처리 여부", example = "false")
    private Boolean isProcessed;

    @Schema(description = "메모", example = "첫 상담: 식단/운동 루틴 안내")
    private String memo;

    public static AdminConsultationResponseDTO from(Consultation c) {
        Customer cust = c.getCustomer();
        return AdminConsultationResponseDTO.builder()
                .id(c.getId())
                .customerName(cust != null ? cust.getName() : null)
                .customerPhoneNumber(cust != null ? cust.getPhoneNumber() : null)
                .date(c.getConsultationDate())
                .time(c.getConsultationTime())
                .createdAt(c.getCreatedAt())
                .isProcessed(c.getIsProcessed())
                .memo(c.getMemo())
                .build();
    }
}
