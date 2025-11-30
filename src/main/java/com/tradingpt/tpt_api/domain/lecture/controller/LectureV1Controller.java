package com.tradingpt.tpt_api.domain.lecture.controller;

import com.tradingpt.tpt_api.domain.lecture.dto.request.LectureProgressUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionHistoryItemDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureAttachmentDownloadResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.service.command.LectureCommandService;
import com.tradingpt.tpt_api.domain.lecture.service.query.LectureQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "무료 강의 토큰으로 구매")
    @PostMapping("/{lectureId}/purchase")
    public ResponseEntity<BaseResponse<Long>> purchaseLecture(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId
    ) {
        Long id = lectureCommandService.purchaseLecture(lectureId, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

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
            summary = "내 과제 제출 이력 조회",
            description = "특정 강의에 대해 내가 제출한 모든 과제 파일 이력을 조회합니다."
    )
    @GetMapping("/{lectureId}/assignments/me")
    public ResponseEntity<BaseResponse<List<AssignmentSubmissionHistoryItemDTO>>> getMyAssignmentHistory(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId,
            HttpServletRequest request
    ) {
        List<AssignmentSubmissionHistoryItemDTO> history =
                lectureQueryService.getMyAssignmentSubmissionHistory(userId, lectureId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(history));
    }

    @Operation(
            summary = "강의 재생 URL 발급",
            description = """
            강의 영상을 재생하기 위한 CloudFront Signed URL을 발급합니다.
            - 해당 유저에게 강의가 열려 있어야 합니다.
            - 클라이언트 IP 기반 정책이 적용되어, URL 복사 공유를 방지합니다.
            - 응답으로 주는 playUrl은 만료시간이 적용됩니다.
            """
    )
    @GetMapping("/{lectureId}/play")
    public ResponseEntity<BaseResponse<LecturePlayResponseDTO>> getLecturePlayUrl(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId,
            HttpServletRequest request
    ) {

        // 클라이언트 IP 추출 (X-Forwarded-For 우선)
        String clientIp = extractClientIp(request);

        LecturePlayResponseDTO dto = lectureQueryService.getLecturePlayUrl(
                userId,
                lectureId,
                clientIp
        );

        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }

    @Operation(
            summary = "강의 첨부파일 다운로드 URL 발급",
            description = """
            특정 강의에 포함된 첨부파일을 다운로드하기 위한 CloudFront Signed URL을 발급합니다.
            - IP 기반 정책이 적용되어 URL 복사·공유를 방지합니다.
            - 다운로드 요청 시 환불 시 사용할 다운로드 이력(LectureAttachmentDownloadHistory)이 기록됩니다.
            """
    )
    @GetMapping("/{lectureId}/attachments/{attachmentId}/download")
    public ResponseEntity<BaseResponse<LectureAttachmentDownloadResponseDTO>> getLectureAttachmentDownloadUrl(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long lectureId,
            @PathVariable Long attachmentId,
            HttpServletRequest request
    ) {

        // 클라이언트 IP 추출
        String clientIp = extractClientIp(request);

        LectureAttachmentDownloadResponseDTO dto = lectureQueryService.getLectureAttachmentDownloadUrl(userId, lectureId, attachmentId,clientIp);

        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }


    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim(); // 첫 번째가 실제 클라이언트
        }
        return request.getRemoteAddr();
    }
}
