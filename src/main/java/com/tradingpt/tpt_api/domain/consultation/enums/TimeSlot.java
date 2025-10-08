package com.tradingpt.tpt_api.domain.consultation.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "상담 가능 시간 슬롯 (정각 단위)")
public enum TimeSlot {
    H09(LocalTime.of(9, 0)),
    H10(LocalTime.of(10, 0)),
    H11(LocalTime.of(11, 0)),
    H13(LocalTime.of(13, 0)),
    H14(LocalTime.of(14, 0)),
    H15(LocalTime.of(15, 0)),
    H16(LocalTime.of(16, 0)),
    H17(LocalTime.of(17, 0)),
    H18(LocalTime.of(18, 0));

    private final LocalTime time;
}
