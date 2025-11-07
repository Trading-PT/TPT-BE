package com.tradingpt.tpt_api.global.infrastructure.s3.response;

public record S3UploadResult(
	String key,
	String url,
	String originalFilename,
	String contentType
) {
}
