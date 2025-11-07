package com.tradingpt.tpt_api.global.infrastructure.s3.response;

/**
 * S3 사전 서명(Presigned) 업로드 요청 결과 DTO.
 * 프론트엔드가 S3에 직접 업로드할 때 필요한 presigned URL, 객체 key, 접근용 public URL을 포함한다.
 */
public record S3PresignedUploadResult(
        String presignedUrl,  // 프론트가 PUT/UPLOAD 요청에 사용할 URL
        String objectKey,     // S3에 저장될 키 (삭제/조회용)
        String publicUrl      // 접근용 URL (CloudFront/버킷 정책에 따라 달라짐)
) {}
