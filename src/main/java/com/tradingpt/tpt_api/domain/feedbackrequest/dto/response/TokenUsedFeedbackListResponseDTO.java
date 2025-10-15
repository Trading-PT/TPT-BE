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
@Schema(description = "토큰 사용 피드백 요청 목록 응답 DTO (무한 스크롤)")
public class TokenUsedFeedbackListResponseDTO {

	@Schema(description = "토큰 사용 피드백 요청 목록")
	private List<TokenUsedFeedbackListItemDTO> feedbacks;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;

	public static TokenUsedFeedbackListResponseDTO of(
		List<TokenUsedFeedbackListItemDTO> feedbacks,
		SliceInfo sliceInfo
	) {
		return TokenUsedFeedbackListResponseDTO.builder()
			.feedbacks(feedbacks)
			.sliceInfo(sliceInfo)
			.build();
	}
}