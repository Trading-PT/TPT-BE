package com.tradingpt.tpt_api.domain.lecture.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "강의 시청 진행도 업데이트 요청 DTO")
public class LectureProgressUpdateRequestDTO {

    @Schema(description = "현재 재생 위치(초)", example = "325")
    private int currentSeconds;
}
