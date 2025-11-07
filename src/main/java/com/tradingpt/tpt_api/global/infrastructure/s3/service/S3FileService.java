package com.tradingpt.tpt_api.global.infrastructure.s3.service;

import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3PresignedUploadResult;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * S3 파일 업로드/삭제 기능을 정의한 추상화 레이어.
 * <p>
 * 외부에서는 이 인터페이스만 의존하게 만들어 구현체 교체가 자유롭도록 한다.
 */
public interface S3FileService {

	/**
	 * 사용자가 업로드한 멀티파트 파일을 S3에 저장한다.
	 *
	 * @param file 업로드할 파일 (이미지, 문서 등)
	 * @param directory 업로드할 상위 디렉터리. 비어 있으면 기본 uploads 경로 사용
	 * @return 업로드 결과(객체 키, 접근 URL 등)
	 */
	S3UploadResult upload(MultipartFile file, String directory);

	/**
	 * 스트림 형태의 데이터를 S3에 저장한다. 백그라운드 작업이나 변환 결과 저장 시 활용.
	 *
	 * @param inputStream 업로드할 데이터 스트림
	 * @param contentLength 스트림 길이(byte)
	 * @param originalFilename 원본 파일명. 확장자 검증에 사용
	 * @param directory 업로드 대상 디렉터리 경로
	 * @return 업로드 결과(객체 키, 접근 URL 등)
	 */
	S3UploadResult upload(InputStream inputStream, long contentLength, String originalFilename, String directory);

	/**
	 * S3에 저장된 객체를 삭제한다.
	 *
	 * @param key S3 객체 키 (파일 경로)
	 */
	void delete(String key);

	/**
	 * 클라이언트가 S3에 직접 업로드할 수 있도록 사전 서명된 URL을 발급한다.
	 *
	 * @param originalFilename 업로드하려는 원본 파일명
	 * @param directory 업로드 대상 디렉터리 (예: "lectures", "images")
	 * @return 프리사인드 URL, 실제 object key, public url 등
	 */
	S3PresignedUploadResult createPresignedUploadUrl(String originalFilename, String directory);
}

