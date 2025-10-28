package com.tradingpt.tpt_api.domain.column.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDTO {

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    @NotBlank(message = "댓글 내용은 필수 입력값입니다.")
    private String content;
}
