package com.tradingpt.tpt_api.domain.memo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "메모 생성/수정 요청 DTO")
public class MemoRequestDTO {

    @NotBlank(message = "메모 제목은 필수입니다.")
    @Size(max = 100, message = "메모 제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "메모 제목", example = "오늘의 트레이딩 포인트")
    private String title;

    @NotBlank(message = "메모 내용은 필수입니다.")
    @Size(max = 5000, message = "메모 내용은 5000자를 초과할 수 없습니다.")
    @Schema(description = "메모 내용", example = "- 손절가 설정 잊지 말기\n- 감정적 트레이딩 주의")
    private String content;
}
