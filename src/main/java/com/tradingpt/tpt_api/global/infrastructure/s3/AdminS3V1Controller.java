package com.tradingpt.tpt_api.global.infrastructure.s3;

import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/s3")
@Validated
@Tag(name = "어드민 - S3 관리", description = "어드민 전용 S3 이미지 업로드 API")
public class AdminS3V1Controller {

    private final S3FileService s3FileService;

    /**
     * 어드민 전용 이미지 업로드 API
     * - 인증된 ADMIN만 접근 가능
     * - multipart/form-data 요청 필드명: file
     * - 업로드 경로: 지정 디렉터리 (없으면 기본 uploads)
     */
    @Operation(summary = "S3 이미지 업로드 (어드민 전용)",
            description = "관리자 전용으로 이미지를 S3에 업로드합니다. 반환값은 업로드된 파일의 URL 및 키 정보입니다.")
    @PostMapping("/uploads")
    public ResponseEntity<BaseResponse<S3UploadResult>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory
    ) {
        String dir = (directory == null || directory.isBlank()) ? "uploads" : directory;
        S3UploadResult result = s3FileService.upload(file, dir);
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
}
