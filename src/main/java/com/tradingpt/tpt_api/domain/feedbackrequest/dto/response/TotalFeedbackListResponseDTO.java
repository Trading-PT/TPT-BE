package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.util.List;

import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "전체 피드백 목록")
public class TotalFeedbackListResponseDTO {

	@Schema(description = "피드백 요청 목록")
	private List<AdminFeedbackCardResponseDTO> adminFeedbackCardResponseDTOS;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;
}
