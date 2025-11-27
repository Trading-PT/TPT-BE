package com.tradingpt.tpt_api.global.infrastructure.s3.response;

import java.time.Duration;
import java.time.Instant;

/**
 * S3 사전 서명(Presigned) 다운로드 URL 결과 DTO.
 * Private 버킷의 파일에 대한 임시 접근 권한을 제공할 때 사용한다.
 *
 * @param presignedUrl 임시 다운로드 URL (만료 시간 후 접근 불가)
 * @param objectKey S3 객체 키
 * @param expiresAt URL 만료 시각 (ISO-8601)
 * @param expirationSeconds 만료까지 남은 시간 (초)
 */
public record S3PresignedDownloadResult(
        String presignedUrl,
        String objectKey,
        Instant expiresAt,
        long expirationSeconds
) {
    public static S3PresignedDownloadResult of(String presignedUrl, String objectKey, Duration expiration) {
        Instant expiresAt = Instant.now().plus(expiration);
        return new S3PresignedDownloadResult(
                presignedUrl,
                objectKey,
                expiresAt,
                expiration.toSeconds()
        );
    }
}
