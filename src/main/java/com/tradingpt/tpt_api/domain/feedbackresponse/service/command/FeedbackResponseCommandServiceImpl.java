package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.CreateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.feedbackresponse.exception.FeedbackResponseErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackresponse.exception.FeedbackResponseException;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3UploadResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 피드백 답변 Command Service 구현체
 * 피드백 답변 생성, 수정과 관련된 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackResponseCommandServiceImpl implements FeedbackResponseCommandService {

	private static final Tika TIKA = new Tika();
	private static final Safelist CONTENT_SAFE_LIST = Safelist.relaxed()
		.addAttributes("a", "target", "rel")
		.addProtocols("a", "href", "http", "https", "mailto")
		.addProtocols("img", "src", "http", "https")
		.addTags("figure", "figcaption", "hr");

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;
	private final S3FileService s3FileService;

	@Override
	public FeedbackResponseDTO createFeedbackResponse(Long feedbackRequestId, CreateFeedbackResponseRequestDTO request,
		Long trainerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		if (feedbackRequest.getFeedbackResponse() != null) { // 이미 피드백 요청에 대해 응답이 있으면 에러를 리턴
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_ALREADY_EXISTS);
		}

		Trainer trainer = getTrainerById(trainerId);

		String sanitizedContent = normalizeContent(feedbackRequestId, request.getContent());

		FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(feedbackRequest, trainer, request.getTitle(),
			sanitizedContent);

		feedbackRequest.setStatus(Status.DONE);

		// FeedbackResponse는 cascade로 저장됨
		FeedbackRequest saved = feedbackRequestRepository.save(feedbackRequest);

		return FeedbackResponseDTO.of(feedbackResponse, trainer);
	}

	@Override
	public void updateFeedbackResponse(Long feedbackRequestId, String responseContent, Long trainerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse();
		if (feedbackResponse == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_NOT_FOUND);
		}

		// 답변 작성자만 수정 가능
		if (!feedbackResponse.getTrainer().getId().equals(trainerId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.RESPONSE_UPDATE_PERMISSION_DENIED);
		}

		String sanitizedContent = normalizeContent(feedbackRequestId, responseContent);
		feedbackResponse.updateContent(sanitizedContent);
		feedbackRequestRepository.save(feedbackRequest);
	}

	private Trainer getTrainerById(Long trainerId) {
		return (Trainer)userRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
	}

	private String normalizeContent(Long feedbackRequestId, String rawContent) {
		if (!StringUtils.hasText(rawContent)) {
			return rawContent;
		}

		Document document = Jsoup.parseBodyFragment(rawContent);
		for (Element image : document.select("img")) {
			String src = image.attr("src");
			if (!StringUtils.hasText(src) || !src.startsWith("data:")) {
				continue;
			}
			String uploadedUrl = uploadInlineImage(src, feedbackRequestId);
			image.attr("src", uploadedUrl);
		}

		return Jsoup.clean(document.body().html(), CONTENT_SAFE_LIST);
	}

	private String uploadInlineImage(String dataUri, Long feedbackRequestId) {
		try {
			int base64Index = dataUri.indexOf("base64,");
			if (base64Index < 0) {
				throw new FeedbackResponseException(FeedbackResponseErrorStatus.INVALID_CONTENT_FORMAT);
			}

			String header = dataUri.substring("data:".length(), base64Index);
			String mimeType = extractMimeType(header);
			String base64Payload = dataUri.substring(base64Index + "base64,".length());
			byte[] decoded = Base64.getDecoder().decode(base64Payload);

			String detectedMime = StringUtils.hasText(mimeType) ? mimeType : TIKA.detect(decoded);
			String extension = resolveExtension(detectedMime);
			if (extension == null) {
				throw new FeedbackResponseException(FeedbackResponseErrorStatus.UNSUPPORTED_IMAGE_TYPE);
			}

			String directory = "feedback-responses/" + feedbackRequestId;
			String originalName = "inline-" + UUID.randomUUID() + "." + extension;
			try (InputStream inputStream = new ByteArrayInputStream(decoded)) {
				S3UploadResult result = s3FileService.upload(inputStream, decoded.length, originalName, directory);
				return result.url();
			}
		} catch (IllegalArgumentException ex) {
			throw new FeedbackResponseException(FeedbackResponseErrorStatus.INVALID_CONTENT_FORMAT);
		} catch (FeedbackResponseException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new FeedbackResponseException(FeedbackResponseErrorStatus.IMAGE_UPLOAD_FAILED);
		}
	}

	private String extractMimeType(String header) {
		if (!StringUtils.hasText(header)) {
			return null;
		}
		int separatorIndex = header.indexOf(';');
		String mime = separatorIndex >= 0 ? header.substring(0, separatorIndex) : header;
		return StringUtils.hasText(mime) ? mime.trim() : null;
	}

	private String resolveExtension(String mimeType) {
		if (!StringUtils.hasText(mimeType)) {
			return null;
		}
		switch (mimeType.toLowerCase()) {
			case "image/jpeg":
			case "image/jpg":
				return "jpg";
			case "image/png":
				return "png";
			default:
				return null;
		}
	}
}
