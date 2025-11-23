package com.tradingpt.tpt_api.domain.event.dto.response;

import com.tradingpt.tpt_api.domain.event.entity.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "이벤트 응답 DTO")
public class EventResponseDTO {

    @Schema(description = "이벤트 ID", example = "1")
    private Long id;

    @Schema(description = "이벤트 이름", example = "10월 가입 이벤트")
    private String name;

    @Schema(description = "이벤트 시작 일시", example = "2025-10-15T00:00:00")
    private LocalDateTime startAt;

    @Schema(description = "이벤트 종료 일시", example = "2025-10-20T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "보상 토큰 개수", example = "5")
    private int tokenAmount;

    @Schema(description = "활성 여부", example = "true")
    private boolean active;

    public static EventResponseDTO from(Event event) {
        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .tokenAmount(event.getTokenAmount())
                .active(event.isActive())
                .build();
    }
}
