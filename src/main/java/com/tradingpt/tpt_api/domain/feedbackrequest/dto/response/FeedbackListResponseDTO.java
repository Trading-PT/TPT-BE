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
@Schema(description = "실시간 트레이딩 피드백 요청 목록 응답 DTO (무한 스크롤)")
public class FeedbackListResponseDTO {

	@Schema(description = "피드백 요청 목록")
	private List<FeedbackCardResponseDTO> feedbacks;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;

	public static FeedbackListResponseDTO of(List<FeedbackCardResponseDTO> feedbacks, SliceInfo sliceInfo) {
		return FeedbackListResponseDTO.builder()
			.feedbacks(feedbacks)
			.sliceInfo(sliceInfo)
			.build();
	}

}
