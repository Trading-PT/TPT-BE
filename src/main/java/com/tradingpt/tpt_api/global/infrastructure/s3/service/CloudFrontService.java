package com.tradingpt.tpt_api.global.infrastructure.s3.service;

import java.time.Duration;

public interface CloudFrontService {
    String createSignedUrl(String objectKey, Duration duration, String clientIp);
}
