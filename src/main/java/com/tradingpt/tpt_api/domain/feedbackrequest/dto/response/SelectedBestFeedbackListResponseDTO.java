package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.util.List;

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
@Schema(description = "현재 선정된 베스트 피드백")
public class SelectedBestFeedbackListResponseDTO {

	private List<AdminFeedbackCardDTO> adminFeedbackCardDTOS; // 최대 3개만 존재하기 때문에 페이징 정보 필요 없음.

	public static SelectedBestFeedbackListResponseDTO from(List<AdminFeedbackCardDTO> adminFeedbackCardDTOS) {
		return SelectedBestFeedbackListResponseDTO.builder()
			.adminFeedbackCardDTOS(adminFeedbackCardDTOS)
			.build();
	}
}
