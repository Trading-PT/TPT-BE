package com.tradingpt.tpt_api.domain.consultation.dto.response;

import com.tradingpt.tpt_api.domain.consultation.enums.TimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "상담 가능 시간대 조회 응답 DTO")
public class SlotAvailabilityDTO {

    @Schema(
            description = "상담 시간대 (09시~18시, 정각 단위)",
            example = "H16"
    )
    private TimeSlot timeSlot;

    @Schema(
            description = "해당 시간대 상담 가능 여부 (true = 예약 가능, false = 예약 불가)",
            example = "true"
    )
    private boolean available;
}
