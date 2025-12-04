package com.tradingpt.tpt_api.domain.lecture.controller;

import com.tradingpt.tpt_api.domain.lecture.dto.LectureListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.request.ChapterCreateRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.CustomerHomeworkSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.request.LectureRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.service.command.AdminChapterCommandService;
import com.tradingpt.tpt_api.domain.lecture.service.command.AdminLectureCommandService;
import com.tradingpt.tpt_api.domain.lecture.service.query.AdminLectureQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3PresignedUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 어드민 전용: 챕터 생성/삭제
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/lectures")
@Tag(name = "어드민-강의관리", description = "어드민 전용 강의/챕터 관리 API")
public class AdminLectureV1Controller {

    private final AdminChapterCommandService adminChapterCommandService;
    private final AdminLectureCommandService adminLectureCommandService;
    private final AdminLectureQueryService adminLectureQueryService;
    private final S3FileService s3FileService;

    @Operation(summary = "챕터 생성(어드민)",
            description = "주차(챕터)를 생성합니다. chapterOrder는 같은 과정 내 정렬용입니다.")
    @PostMapping("/chapters")
    public ResponseEntity<BaseResponse<Long>> createChapter(
            @Valid @RequestBody ChapterCreateRequestDTO request) {

        Long id = adminChapterCommandService.createChapter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.onSuccessCreate(id));
    }

    @Operation(summary = "챕터 전체 조회(어드민)",
            description = "모든 챕터를 조회합니다. 정렬은 chapterOrder ASC 기준입니다.")
    @GetMapping("/chapters/all")
    public ResponseEntity<BaseResponse<List<ChapterListResponseDTO>>> getAllChapters() {

        List<ChapterListResponseDTO> chapters = adminLectureQueryService.getAllChapters();
        return ResponseEntity.ok(BaseResponse.onSuccess(chapters));
    }

    @Operation(
            summary = "챕터 수정(어드민)",
            description = "기존 챕터의 정보를 수정합니다. chapterOrder, 제목, 설명 등 변경 가능합니다."
    )
    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<BaseResponse<Long>> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterCreateRequestDTO request
    ) {
        Long updatedId = adminChapterCommandService.updateChapter(chapterId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(updatedId));
    }


    @Operation(summary = "챕터 삭제(어드민)",
            description = "해당 챕터를 삭제합니다. 운영정책에 따라 물리삭제/소프트삭제를 서비스에서 결정하세요.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<BaseResponse<Void>> deleteChapter(@PathVariable Long chapterId) {
        adminChapterCommandService.deleteChapter(chapterId);
        return ResponseEntity.ok(BaseResponse.onSuccess(null));
    }

    @Operation(summary = "강의 생성(어드민)",
            description = "지정한 챕터에 강의를 등록합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createLecture(
            @AuthenticationPrincipal(expression = "id") Long trainerId,
            @Valid @RequestBody LectureRequestDTO request) {

        Long lectureId = adminLectureCommandService.createLecture(request, trainerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.onSuccessCreate(lectureId));
    }

    @Operation(summary = "강의 목록 조회(어드민)",
            description = "관리자 화면의 '강의 목록' 테이블 데이터입니다. category=All 혹은 enum string으로 넘기세요.")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<LectureListResponseDTO>>> getLectures(
            @RequestParam(name = "category", defaultValue = "All") String category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LectureListResponseDTO> page = adminLectureQueryService.getLectureList(pageable, category);
        return ResponseEntity.ok(BaseResponse.onSuccess(page));
    }


    @Operation(summary = "강의 상세 조회(어드민)",
            description = "관리자 화면의 '강의 상세'에 들어갈 데이터입니다.")
    @GetMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<LectureDetailResponseDTO>> getLectureDetail(@PathVariable Long lectureId) {

        LectureDetailResponseDTO dto = adminLectureQueryService.getLectureDetail(lectureId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }


    @Operation(summary = "강의 수정(어드민)",
            description = "기존 강의의 모든 필드를 수정합니다. videoUrl/videoKey, 첨부파일들도 전부 갈아끼울 수 있습니다.")
    @PutMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<Long>> updateLecture(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal(expression = "id") Long trainerId,
            @Valid @RequestBody LectureRequestDTO request
    ) {
        Long updatedId = adminLectureCommandService.updateLecture(lectureId, request, trainerId);
        return ResponseEntity.ok(BaseResponse.onSuccess(updatedId));
    }

    @Operation(summary = "강의 삭제(어드민)",
            description = "해당 강의를 삭제합니다.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<Void>> deleteLecture(@PathVariable Long lectureId) {
        adminLectureCommandService.deleteLecture(lectureId);
        return ResponseEntity.ok(BaseResponse.onSuccess(null));
    }

    @Operation(summary = "동영상 업로드용 Presigned URL 발급", description = "프론트가 이 URL로 S3에 직접 업로드합니다.")
    @PostMapping("/uploads/presigned")
    public ResponseEntity<BaseResponse<S3PresignedUploadResult>> createPresignedUrl(
            @RequestParam String filename,
            @RequestParam(defaultValue = "lectures") String directory) {

        var result = s3FileService.createPresignedUploadUrl(filename, directory);
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }

    @Operation(summary = "특정 회원에게 강의 오픈(어드민)",
            description = "관리자가 특정 강의를 특정 고객에게 수동으로 오픈합니다.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{lectureId}/open")
    public ResponseEntity<BaseResponse<Long>> openLectureForCustomer(
            @PathVariable Long lectureId,
            @RequestParam("customerId") Long customerId
    ) {
        Long openedLectureId = adminLectureCommandService.openLecture(lectureId, customerId);
        return ResponseEntity.ok(BaseResponse.onSuccess(openedLectureId));
    }

    @Operation(
            summary = "특정 회원 과제 현황 조회(어드민)",
            description = " 내 담당 고객 관리 > 과제 관리 화면에 필요한 데이터를 반환합니다."
    )
    @GetMapping("/customers/{customerId}/homeworks")
    public ResponseEntity<BaseResponse<CustomerHomeworkSummaryResponseDTO>> getCustomerHomeworkStatus(
            @PathVariable Long customerId
    ) {
        CustomerHomeworkSummaryResponseDTO dto = adminLectureQueryService.getCustomerHomeworkSummary(customerId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }
}
