package com.tradingpt.tpt_api.global.infrastructure.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.tradingpt.tpt_api.global.infrastructure.content.exception.ContentErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.content.exception.ContentException;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import com.tradingpt.tpt_api.global.util.HtmlSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 콘텐츠 내 인라인 이미지 업로드 서비스
 * - HTML 콘텐츠 내 data URI 형식의 이미지를 S3에 업로드
 * - 업로드된 이미지 URL로 변환
 * - HTML sanitization 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentImageUploader {

	private static final Tika TIKA = new Tika();
	private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB

	private final S3FileService s3FileService;

	/**
	 * HTML 콘텐츠를 처리합니다.
	 * 1. data URI 형식의 인라인 이미지를 S3에 업로드
	 * 2. 업로드된 URL로 변환
	 * 3. HTML sanitization 적용 (XSS 방지)
	 *
	 * @param rawContent   원본 HTML 콘텐츠
	 * @param resourceType 리소스 타입 (예: "feedback-responses", "monthly-summaries")
	 * @return 처리 완료된 HTML 콘텐츠
	 */
	public String processContent(String rawContent, String resourceType) {
		if (!StringUtils.hasText(rawContent)) {
			log.debug("Empty content provided for processing");
			return rawContent;
		}

		log.debug("Processing content for resourceType={}", resourceType);

		// 1. 인라인 이미지가 있는지 확인
		if (!HtmlSanitizer.hasInlineImages(rawContent)) {
			log.debug("No inline images found, only sanitizing");
			return HtmlSanitizer.sanitize(rawContent);
		}

		// 2. 인라인 이미지를 S3에 업로드하고 URL로 변환
		String processedHtml = uploadInlineImages(rawContent, resourceType);

		// 3. HTML sanitize (XSS 방지)
		String sanitized = HtmlSanitizer.sanitize(processedHtml);

		log.debug("Content processing completed: {} inline images uploaded",
			countImages(rawContent));

		return sanitized;
	}

	/**
	 * HTML 내 모든 data URI 형식의 이미지를 S3에 업로드하고 URL로 변환합니다.
	 *
	 * @param html         원본 HTML
	 * @param resourceType 리소스 타입
	 * @return 이미지 URL이 변환된 HTML
	 */
	private String uploadInlineImages(String html, String resourceType) {
		Document document = Jsoup.parseBodyFragment(html);
		int uploadCount = 0;
		int failCount = 0;

		for (Element image : document.select("img")) {
			String src = image.attr("src");

			// data URI가 아니면 스킵
			if (!StringUtils.hasText(src) || !src.startsWith("data:")) {
				continue;
			}

			try {
				String uploadedUrl = uploadDataUri(src, resourceType);
				image.attr("src", uploadedUrl);
				uploadCount++;
				log.debug("Inline image {} uploaded successfully", uploadCount);
			} catch (Exception e) {
				failCount++;
				log.error("Failed to upload inline image {}, keeping original data URI. Error: {}",
					uploadCount + failCount, e.getMessage());
				// 업로드 실패 시 원본 data URI 유지
			}
		}

		log.info("Image upload completed for resourceType={}: success={}, failed={}",
			resourceType, uploadCount, failCount);

		return document.body().html();
	}

	/**
	 * data URI를 파싱하여 S3에 업로드합니다.
	 *
	 * @param dataUri      data URI 형식의 이미지 (예: data:image/png;base64,iVBORw0KGgo...)
	 * @param resourceType 리소스 타입
	 * @return 업로드된 이미지의 S3 URL
	 * @throws ContentException 업로드 실패 시
	 */
	private String uploadDataUri(String dataUri, String resourceType) {
		try {
			// 1. Base64 구분자 찾기
			int base64Index = dataUri.indexOf("base64,");
			if (base64Index < 0) {
				log.error("Invalid data URI format: no base64 marker found");
				throw new ContentException(ContentErrorStatus.INVALID_CONTENT_FORMAT);
			}

			// 2. MIME 타입과 Base64 데이터 추출
			String header = dataUri.substring("data:".length(), base64Index);
			String declaredMimeType = extractMimeType(header);
			String base64Payload = dataUri.substring(base64Index + "base64,".length());

			// 3. Base64 디코딩
			byte[] decoded;
			try {
				decoded = Base64.getDecoder().decode(base64Payload);
			} catch (IllegalArgumentException e) {
				log.error("Invalid Base64 encoding", e);
				throw new ContentException(ContentErrorStatus.INVALID_BASE64_FORMAT);
			}

			// 4. 파일 크기 검증
			if (decoded.length > MAX_IMAGE_SIZE) {
				log.error("Image size {} exceeds maximum allowed size {}",
					decoded.length, MAX_IMAGE_SIZE);
				throw new ContentException(ContentErrorStatus.IMAGE_SIZE_EXCEEDED);
			}

			// 5. 실제 MIME 타입 검증 (Tika 사용)
			String detectedMimeType = StringUtils.hasText(declaredMimeType)
				? declaredMimeType
				: TIKA.detect(decoded);

			log.debug("Image MIME type: declared={}, detected={}",
				declaredMimeType, detectedMimeType);

			// 6. 파일 확장자 결정
			String extension = resolveExtension(detectedMimeType);
			if (extension == null) {
				log.error("Unsupported image type: {}", detectedMimeType);
				throw new ContentException(ContentErrorStatus.UNSUPPORTED_IMAGE_TYPE);
			}

			// 7. S3 업로드 (UUID 기반 유니크 경로)
			String directory = resourceType;
			String filename = "inline-" + UUID.randomUUID() + "." + extension;

			try (InputStream inputStream = new ByteArrayInputStream(decoded)) {
				S3UploadResult result = s3FileService.upload(
					inputStream,
					decoded.length,
					filename,
					directory
				);

				log.info("Inline image uploaded successfully: {} -> {}", filename, result.url());
				return result.url();
			}

		} catch (ContentException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Unexpected error during image upload", ex);
			throw new ContentException(ContentErrorStatus.IMAGE_UPLOAD_FAILED);
		}
	}

	/**
	 * data URI 헤더에서 MIME 타입을 추출합니다.
	 *
	 * @param header data URI 헤더 (예: "image/png;charset=utf-8")
	 * @return MIME 타입 (예: "image/png") 또는 null
	 */
	private String extractMimeType(String header) {
		if (!StringUtils.hasText(header)) {
			return null;
		}

		// 세미콜론 이전까지가 MIME 타입
		int separatorIndex = header.indexOf(';');
		String mime = separatorIndex >= 0
			? header.substring(0, separatorIndex)
			: header;

		return StringUtils.hasText(mime) ? mime.trim() : null;
	}

	/**
	 * MIME 타입에서 파일 확장자를 결정합니다.
	 * 지원하는 이미지 포맷: JPEG, PNG, GIF, WebP
	 *
	 * @param mimeType MIME 타입
	 * @return 파일 확장자 또는 null (지원하지 않는 타입)
	 */
	private String resolveExtension(String mimeType) {
		if (!StringUtils.hasText(mimeType)) {
			return null;
		}

		return switch (mimeType.toLowerCase()) {
			case "image/jpeg", "image/jpg" -> "jpg";
			case "image/png" -> "png";
			case "image/gif" -> "gif";
			case "image/webp" -> "webp";
			default -> {
				log.warn("Unsupported MIME type: {}", mimeType);
				yield null;
			}
		};
	}

	/**
	 * HTML 내 이미지 개수를 계산합니다.
	 *
	 * @param html HTML 콘텐츠
	 * @return 이미지 개수
	 */
	private int countImages(String html) {
		if (!StringUtils.hasText(html)) {
			return 0;
		}
		Document document = Jsoup.parseBodyFragment(html);
		return document.select("img[src^='data:']").size();
	}
}