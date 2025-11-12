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
@Schema(description = "트레이너 작성 매매일지 목록 응답 DTO (무한 스크롤)")
public class TrainerWrittenFeedbackListResponseDTO {

    @Schema(description = "트레이너 작성 매매일지 목록")
    private List<TrainerWrittenFeedbackItemDTO> feedbacks;

    @Schema(description = "슬라이스 정보")
    private SliceInfo sliceInfo;

    public static TrainerWrittenFeedbackListResponseDTO of(
        List<TrainerWrittenFeedbackItemDTO> feedbacks,
        SliceInfo sliceInfo
    ) {
        return TrainerWrittenFeedbackListResponseDTO.builder()
            .feedbacks(feedbacks)
            .sliceInfo(sliceInfo)
            .build();
    }
}
