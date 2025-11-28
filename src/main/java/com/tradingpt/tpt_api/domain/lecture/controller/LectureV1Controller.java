package com.tradingpt.tpt_api.domain.lecture.controller;

import com.tradingpt.tpt_api.domain.lecture.dto.request.LectureProgressUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
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
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "강의 시청 진행도 업데이트",
            description = "동영상 플레이어에서 일정 주기마다 현재 재생 위치(초)를 보내면, " +
                    "서버에서 누적 시청 시간, 마지막 재생 위치, 마지막 시청 시각을 갱신하고 필요시 완강 처리합니다.")
    @PatchMapping("/{lectureId}/progress")
    public ResponseEntity<BaseResponse<Void>> updateProgress(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId,
            @RequestBody LectureProgressUpdateRequestDTO request
    ) {
        lectureCommandService.updateLectureProgress(userId, lectureId, request.getCurrentSeconds());
        return ResponseEntity.ok(BaseResponse.onSuccess(null));
    }

    @Operation(
            summary = "과제 제출(PDF)",
            description = "해당 강의의 과제를 파일로 제출합니다. " +
                    "제출 시 유저별 과제 엔티티가 생성/갱신되고, 첨부파일은 S3에 업로드됩니다."
    )
    @PostMapping(value = "/{lectureId}/assignments/submit", consumes = "multipart/form-data")
    public ResponseEntity<BaseResponse<Long>> submitAssignment(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId,
            @RequestPart("file") MultipartFile file
    ) {
        Long submissionId = lectureCommandService.submitAssignment(userId, lectureId, file);
        return ResponseEntity.ok(BaseResponse.onSuccess(submissionId));
    }

    @Operation(
            summary = "본인 과제 제출 상세 조회",
            description = "로그인한 유저가 해당 강의에 제출한 과제 정보를 조회합니다. " +
                    "제출 이력(제출 여부, 제출일시, 첨부파일 URL 등)을 반환합니다."
    )
    @GetMapping("/{lectureId}/assignments/me")
    public ResponseEntity<BaseResponse<AssignmentSubmissionDetailDTO>> getMyAssignment(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId
    ) {
        AssignmentSubmissionDetailDTO dto = lectureQueryService.getMyAssignmentDetail(userId, lectureId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }

    @Operation(
            summary = "강의 재생 URL 발급",
            description = """
                강의 영상을 재생하기 위한 S3 GET Presigned URL을 발급합니다.
                - 해당 유저에게 강의가 열려 있어야 합니다.
                - 응답으로 주는 playUrl은 약 3시간 동안만 유효합니다.
                """
    )
    @GetMapping("/{lectureId}/play")
    public ResponseEntity<BaseResponse<LecturePlayResponseDTO>> getLecturePlayUrl(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId
    ) {
        LecturePlayResponseDTO dto = lectureQueryService.getLecturePlayUrl(userId, lectureId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }
}
