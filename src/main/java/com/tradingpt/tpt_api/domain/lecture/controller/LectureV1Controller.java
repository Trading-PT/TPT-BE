package com.tradingpt.tpt_api.domain.lecture.controller;

import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.service.command.LectureCommandService;
import com.tradingpt.tpt_api.domain.lecture.service.query.LectureQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "유저-강의", description = "유저 전용 강의")
public class LectureV1Controller {

    private final LectureCommandService lectureCommandService;
    private final LectureQueryService lectureQueryService;

    @Operation(summary = "전체 커리큘럼 조회 (무료+유료 통합)")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ChapterBlockDTO>>> getCurriculum(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Parameter(description = "페이지 번호 (0-base)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (챕터 개수)") @RequestParam(defaultValue = "10") int size
    ) {
        List<ChapterBlockDTO> result = lectureQueryService.getCurriculum(userId, page, size);
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }

    @Operation(summary = "무료 강의 토큰으로 구매")
    @PostMapping("/{lectureId}/purchase")
    public ResponseEntity<BaseResponse<Long>> purchaseLecture(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId
    ) {
        Long id = lectureCommandService.purchaseLecture(lectureId, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "강의 상세 조회 (진행도 포함)")
    @GetMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<LectureDetailDTO>> getLectureDetail(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId
    ) {
        LectureDetailDTO result = lectureQueryService.getLectureDetail(userId, lectureId);
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
}
