package com.tradingpt.tpt_api.global.infrastructure.s3.service;

import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3PresignedDownloadResult;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3PresignedUploadResult;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.global.infrastructure.s3.exception.S3ErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.s3.exception.S3Exception;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3 SDK(v2)를 통해 실제 파일 업로드/삭제를 수행하는 구현체.
 * <p>
 * - 모든 입력값은 업로드 시점에 검증하여 서버단에서 발생할 수 있는 예외를 최소화한다.
 * - AWS SDK가 던지는 예외는 {@link S3Exception} 으로 감싸 전역 에러 핸들러와 연동한다.
 * - 디렉터리/파일명 규칙을 한 곳에서 관리해 일관된 저장 경로를 만든다.
 */
@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

	/** 허용되는 확장자(스크린샷, PDF, 한글, 엑셀)를 미리 정의한다. */
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf", "hwp", "xls", "xlsx","mp4", "mov", "m4v");

	/** 확장자별 기본 Content-Type; 클라이언트가 보내지 않은 경우 사용. */
	private static final Map<String, String> CONTENT_TYPE_OVERRIDES = Map.of(
		"jpg", "image/jpeg",
		"jpeg", "image/jpeg",
		"png", "image/png",
		"pdf", "application/pdf",
		"xls", "application/vnd.ms-excel",
		"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		"hwp", "application/x-hwp",
			"mp4", "video/mp4",
			"mov", "video/quicktime",
			"m4v", "video/x-m4v"
	);

	/** Spring Bean 으로 주입받는 S3 클라이언트 (스레드 세이프). */
	private final S3Client s3Client;

	private final S3Presigner s3Presigner;

	/** 환경별 버킷 이름. application-*.yml 에서 주입된다. */
	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	/**
	 * 사용자의 멀티파트 파일을 S3에 업로드한다.
	 * 업로드는 ① 입력 검증 → ② 키 생성 → ③ SDK 호출 순으로 진행된다.
	 */
	@Override
	public S3UploadResult upload(MultipartFile file, String directory) {
		if (file == null || file.isEmpty()) { // 업로드 요청에 파일이 없으면 즉시 실패 처리
			throw new S3Exception(S3ErrorStatus.EMPTY_FILE);
		}

		String originalFilename = file.getOriginalFilename();
		String extension = resolveExtension(originalFilename); // 확장자 추출 및 검증
		validateExtension(extension);

		String contentType = resolveContentType(file.getContentType(), extension); // 실제 업로드 시 사용할 MIME
		String key = buildObjectKey(directory, extension); // 날짜/UUID 기반 고유 경로 생성

		PutObjectRequest request = createPutObjectRequest(key, contentType, file.getSize());

		// Stream 을 열어 실제 S3로 전송한다. try-with-resources 로 스트림 누수를 방지.
		try (InputStream inputStream = file.getInputStream()) {
			uploadToS3(request, RequestBody.fromInputStream(inputStream, file.getSize()));
			return buildResult(key, originalFilename, contentType);
		} catch (IOException ioException) {
			// 파일 스트림을 읽는 과정에서 문제가 발생한 경우 업로드 실패로 변환
			throw new S3Exception(S3ErrorStatus.UPLOAD_FAILED);
		}
	}

	/**
	 * 서버 내부 로직에서 생성한 스트림(예: 리사이즈, 변환 결과)을 S3에 저장할 때 사용하는 API.
	 */
	@Override
	public S3UploadResult upload(InputStream inputStream, long contentLength, String originalFilename,
		String directory) {
		if (inputStream == null || contentLength <= 0) { // 길이를 모르면 AWS SDK가 업로드를 거부하므로 선제 차단
			throw new S3Exception(S3ErrorStatus.INVALID_CONTENT);
		}

		String extension = resolveExtension(originalFilename);
		validateExtension(extension);

		String contentType = resolveContentType(null, extension);
		String key = buildObjectKey(directory, extension);

		PutObjectRequest request = createPutObjectRequest(key, contentType, contentLength);

		uploadToS3(request, RequestBody.fromInputStream(inputStream, contentLength));
		return buildResult(key, originalFilename, contentType);
	}

	@Override
	public S3PresignedUploadResult createPresignedUploadUrl(String originalFilename, String directory) {
		// 1. 파일명/확장자 검증
		String extension = resolveExtension(originalFilename);
		validateExtension(extension);

		// 2. 업로드될 S3 key 생성
		String key = buildObjectKey(directory, extension);

		// 3. content-type 결정
		String contentType = resolveContentType(null, extension);

		// 4. presign 할 PutObjectRequest 만들기
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(contentType)
				.build();

		// 5. 만료시간 지정해서 사전서명 URL 생성 (예: 10분)
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))
				.putObjectRequest(putObjectRequest)
				.build();

		var presigned = s3Presigner.presignPutObject(presignRequest);

		// 6. 클라이언트가 업로드 후 접근할 public URL도 만들어줌
		URL objectUrl = s3Client.utilities()
				.getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build());

		return new S3PresignedUploadResult(
				presigned.url().toString(),
				key,
				objectUrl.toString()
		);
	}

	/**
	 * 업로드된 객체를 삭제한다. 키 유효성 검증 후 SDK 예외를 도메인 예외로 변환한다.
	 */
	@Override
	public void delete(String key) {
		if (!StringUtils.hasText(key)) {
			throw new S3Exception(S3ErrorStatus.INVALID_OBJECT_KEY);
		}

		DeleteObjectRequest request = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.build();

		try {
			s3Client.deleteObject(request);
		} catch (AwsServiceException | SdkClientException sdkException) {
			throw new S3Exception(S3ErrorStatus.DELETE_FAILED);
		}
	}

	@Override
	public String createPresignedGetUrl(String key, Duration duration) {
		if (!StringUtils.hasText(key)) {
			throw new S3Exception(S3ErrorStatus.INVALID_OBJECT_KEY);
		}

		// duration null 들어오면 기본값 3시간 같은 걸로 세팅해도 됨
		Duration effectiveDuration = (duration != null) ? duration : Duration.ofHours(3);

		// 1. GET 요청 정보
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();

		// 2. 사전 서명 설정
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(effectiveDuration)
				.getObjectRequest(getObjectRequest)
				.build();

		// 3. URL 생성 후 문자열로 반환
		return s3Presigner.presignGetObject(presignRequest)
				.url()
				.toString();
	}

	/**
	 * S3에 저장된 파일에 대한 임시 다운로드 URL을 발급한다.
	 * Private 버킷의 파일에 대해 일정 시간 동안만 접근 가능한 URL을 생성한다.
	 *
	 * <p>사용 예시:
	 * <ul>
	 *   <li>강의 영상 조회: 2-4시간 (스트리밍 시간 고려)</li>
	 *   <li>피드백 첨부파일: 30분-1시간</li>
	 *   <li>결제 영수증: 5-15분 (민감 정보)</li>
	 * </ul>
	 *
	 * @param objectKey S3 객체 키 (파일 경로)
	 * @param expirationMinutes URL 만료 시간 (분, 최소 1분, 최대 7일)
	 * @return 프리사인드 다운로드 URL 및 메타데이터
	 */
	@Override
	public S3PresignedDownloadResult createPresignedDownloadUrl(String objectKey, int expirationMinutes) {
		if (!StringUtils.hasText(objectKey)) {
			throw new S3Exception(S3ErrorStatus.INVALID_OBJECT_KEY);
		}

		// 만료 시간 범위 검증 (최소 1분, 최대 7일 = 10080분)
		int validExpiration = Math.max(1, Math.min(expirationMinutes, 10080));
		Duration expiration = Duration.ofMinutes(validExpiration);

		// GetObject 요청 생성
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(objectKey)
			.build();

		// Presigned URL 생성
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(expiration)
			.getObjectRequest(getObjectRequest)
			.build();

		try {
			var presigned = s3Presigner.presignGetObject(presignRequest);
			return S3PresignedDownloadResult.of(
				presigned.url().toString(),
				objectKey,
				expiration
			);
		} catch (AwsServiceException | SdkClientException sdkException) {
			throw new S3Exception(S3ErrorStatus.PRESIGN_FAILED);
		}
	}



	/**
	 * 공통 업로드 로직. SDK 호출부만 분리해 예외 처리를 한 곳에서 관리한다.
	 */
	private void uploadToS3(PutObjectRequest request, RequestBody body) {
		try {
			s3Client.putObject(request, body);
		} catch (AwsServiceException | SdkClientException sdkException) {
			throw new S3Exception(S3ErrorStatus.UPLOAD_FAILED);
		}
	}

	/**
	 * AWS SDK에 전달할 PutObjectRequest 생성 도우미.
	 */
	private PutObjectRequest createPutObjectRequest(String key, String contentType, long contentLength) {
		return PutObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.contentType(contentType)
			.contentLength(contentLength)
			.build();
	}

	/**
	 * 업로드 완료 후 API 응답에 사용할 메타데이터를 조립한다.
	 */
	private S3UploadResult buildResult(String key, String originalFilename, String contentType) {
		URL objectUrl = s3Client.utilities()
			.getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build());
		return new S3UploadResult(key, objectUrl.toString(), originalFilename, contentType);
	}

	/**
	 * 업로드할 S3 객체 키를 생성한다.
	 * 디렉터리 문자열을 정제하고 날짜/UUID를 조합해 충돌 가능성을 줄인다.
	 */
	private String buildObjectKey(String directory, String extension) {
		String normalizedDirectory = StringUtils.hasText(directory) ? directory.trim() : "uploads";
		normalizedDirectory = normalizedDirectory.replaceAll("^/+", "").replaceAll("/+", "/").replaceAll("/+$", "");
		if (!StringUtils.hasText(normalizedDirectory)) {
			normalizedDirectory = "uploads";
		}

		String datePath = LocalDate.now().toString();
		String randomName = UUID.randomUUID().toString().replace("-", "");
		return normalizedDirectory + "/" + datePath + "/" + randomName + "." + extension;
	}

	/**
	 * 화이트리스트 외의 확장자는 업로드되지 않도록 선제 차단한다.
	 */
	private void validateExtension(String extension) {
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new S3Exception(S3ErrorStatus.UNSUPPORTED_EXTENSION);
		}
	}

	/**
	 * 마지막 점(.)을 기준으로 확장자를 추출하여 소문자로 변환한다.
	 */
	private String resolveExtension(String originalFilename) {
		if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
			throw new S3Exception(S3ErrorStatus.INVALID_FILENAME);
		}
		return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
	}

	/**
	 * 업로드 시 적용할 Content-Type을 결정한다.
	 * 클라이언트가 명시한 값이 있으면 우선 사용하고, 없으면 우리가 정의한 매핑으로 보완한다.
	 */
	private String resolveContentType(String providedContentType, String extension) {
		if (StringUtils.hasText(providedContentType) && !"application/octet-stream".equals(providedContentType)) {
			return providedContentType;
		}
		return CONTENT_TYPE_OVERRIDES.getOrDefault(extension, "application/octet-stream");
	}
}
