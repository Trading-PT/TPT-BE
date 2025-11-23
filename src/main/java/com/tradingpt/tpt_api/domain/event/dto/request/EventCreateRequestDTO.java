package com.tradingpt.tpt_api.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "이벤트 생성 요청 DTO")
public class EventCreateRequestDTO {

    @NotBlank
    @Schema(description = "이벤트 이름", example = "10월 가입 이벤트")
    private String name;

    @NotNull
    @Schema(description = "이벤트 시작 일시", example = "2025-10-15T00:00:00")
    private LocalDateTime startAt;

    @NotNull
    @Schema(description = "이벤트 종료 일시", example = "2025-10-20T23:59:59")
    private LocalDateTime endAt;

    @NotNull
    @Schema(description = "보상 토큰 개수", example = "5")
    private Integer tokenAmount;
}
