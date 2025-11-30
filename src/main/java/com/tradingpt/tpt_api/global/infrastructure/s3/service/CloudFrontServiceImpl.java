package com.tradingpt.tpt_api.global.infrastructure.s3.service;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;


@Service
@RequiredArgsConstructor
public class CloudFrontServiceImpl implements CloudFrontService {

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${cloudfront.keyPairId}")
    private String keyPairId;

    @Value("${cloudfront.privateKey}")
    private String privateKeyPem; // ----BEGIN PRIVATE KEY---- 형태의 문자열 전체 주입

    /**
     * CloudFront Signed URL 생성 + IP 제한
     *
     * @param objectKey  S3/CloudFront 경로 (예: "videos/lecture1/index.m3u8")
     * @param duration   URL 유효 시간
     * @param clientIp   허용할 클라이언트 IP (예: "123.123.123.123")
     */
    @Override
    public String createSignedUrl(String objectKey, Duration duration, String clientIp) {
        try {
            long expiresMillis = System.currentTimeMillis() + duration.toMillis();
            long expiresEpoch = expiresMillis / 1000L;

            String resourceUrl = String.format("https://%s/%s", cloudFrontDomain, objectKey);
            PrivateKey privateKey = loadPrivateKey(privateKeyPem);

            String policy = String.format("""
        {
          "Statement": [
            {
              "Resource": "%s",
              "Condition": {
                "DateLessThan": { "AWS:EpochTime": %d }
              }
            }
          ]
        }
        """, resourceUrl, expiresEpoch);

            return CloudFrontUrlSigner.getSignedURLWithCustomPolicy(
                    resourceUrl,
                    keyPairId,
                    privateKey,
                    policy
            );
        } catch (Exception e) {
            throw new RuntimeException("CloudFront Signed URL 생성 실패", e);
        }
    }


    // PEM 문자열을 PrivateKey 객체로 변환
    private PrivateKey loadPrivateKey(String pem) {
        try {
            // PEM 헤더/푸터 제거
            String cleaned = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleaned);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("CloudFront private key 파싱 실패", e);
        }
    }
}
