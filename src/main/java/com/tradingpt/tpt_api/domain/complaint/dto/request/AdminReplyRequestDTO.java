package com.tradingpt.tpt_api.domain.complaint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminReplyRequestDTO {

	@Schema(description = "관리자 답변 내용",
		example = "고객님 불편을 드려 죄송합니다. 내부적으로 즉시 조치하겠습니다.")
	@NotBlank(message = "답변 내용은 필수입니다.")
	private String reply;
}
