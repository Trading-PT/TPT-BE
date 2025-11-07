package com.tradingpt.tpt_api.domain.leveltest.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestGradeRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestMultipleChoiceRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubjectiveRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LevelTestQuestionRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminLevelTestCommandServiceImpl implements AdminLeveltestCommandService {

	private static final String LEVELTEST_DIR = "leveltests/";
	private final LevelTestQuestionRepository leveltestQuestionRepository;
	private final LeveltestAttemptRepository leveltestAttemptRepository;
	private final LeveltestResponseRepository leveltestResponseRepository;
	private final S3FileService s3FileService;

	@Override
	@Transactional
	public LeveltestQuestionResponseDTO createMultipleChoiceQuestion(
		LeveltestMultipleChoiceRequestDTO req, MultipartFile image
	) {
		String imageKey = null;
		String imageUrl = null;

		if (image != null && !image.isEmpty()) {
			var uploaded = s3FileService.upload(image, LEVELTEST_DIR);
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		}

		LevelTestQuestion entity = LevelTestQuestion.builder()
			.content(req.getContent())
			.score(req.getScore())
			.problemType(ProblemType.MULTIPLE_CHOICE)
			.imageKey(imageKey)
			.imageUrl(imageUrl)
			.choice1(req.getChoice1())
			.choice2(req.getChoice2())
			.choice3(req.getChoice3())
			.choice4(req.getChoice4())
			.choice5(req.getChoice5())
			.correctChoiceNum(req.getCorrectChoiceNum())
			.build();

		LevelTestQuestion saved = leveltestQuestionRepository.save(entity);

		return LeveltestQuestionResponseDTO.builder()
			.questionId(saved.getId())
			.imageUrl(saved.getImageUrl())
			.build();
	}

	@Override
	@Transactional
	public LeveltestQuestionResponseDTO createTextAnswerQuestion(
		LeveltestSubjectiveRequestDTO req, MultipartFile image
	) {

		String imageKey = null;
		String imageUrl = null;

		if (image != null && !image.isEmpty()) {
			var uploaded = s3FileService.upload(image, LEVELTEST_DIR);
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		}

		LevelTestQuestion entity = LevelTestQuestion.builder()
			.content(req.getContent())
			.score(req.getScore())
			.problemType(req.getProblemType())
			.imageKey(imageKey)
			.imageUrl(imageUrl)
			.answerText(req.getAnswerText())
			.build();

		LevelTestQuestion saved = leveltestQuestionRepository.save(entity);

		return LeveltestQuestionResponseDTO.builder()
			.questionId(saved.getId())
			.imageUrl(saved.getImageUrl())
			.build();
	}

	@Override
	@Transactional
	public LeveltestQuestionResponseDTO updateMultipleChoiceQuestion(
		Long questionId, LeveltestMultipleChoiceRequestDTO req, MultipartFile image
	) {
		LevelTestQuestion question = leveltestQuestionRepository.findById(questionId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND));

		String imageKey = question.getImageKey();
		String imageUrl = question.getImageUrl();

		if (image != null && !image.isEmpty()) {
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			var uploaded = s3FileService.upload(image, LEVELTEST_DIR);
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		} else {
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			imageKey = null;
			imageUrl = null;
		}
		question.changeImage(imageKey, imageUrl);

		question.changeContent(req.getContent());
		question.changeScore(req.getScore());
		question.changeProblemType(ProblemType.MULTIPLE_CHOICE);
		question.changeChoices(req.getChoice1(), req.getChoice2(), req.getChoice3(), req.getChoice4(),
			req.getChoice5());
		question.changeCorrectChoiceNum(req.getCorrectChoiceNum());

		return LeveltestQuestionResponseDTO.builder()
			.questionId(question.getId())
			.imageUrl(question.getImageUrl())
			.build();
	}

	@Override
	@Transactional
	public LeveltestQuestionResponseDTO updateTextAnswerQuestion(
		Long questionId, LeveltestSubjectiveRequestDTO req, MultipartFile image
	) {

		LevelTestQuestion question = leveltestQuestionRepository.findById(questionId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND));

		String imageKey = question.getImageKey();
		String imageUrl = question.getImageUrl();

		if (image != null && !image.isEmpty()) {
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			var uploaded = s3FileService.upload(image, LEVELTEST_DIR);
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		} else {
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			imageKey = null;
			imageUrl = null;
		}
		question.changeImage(imageKey, imageUrl);

		question.changeContent(req.getContent());
		question.changeScore(req.getScore());
		question.changeProblemType(req.getProblemType()); // SHORT_ANSWER or SUBJECTIVE
		question.changeAnswerText(req.getAnswerText());

		return LeveltestQuestionResponseDTO.builder()
			.questionId(question.getId())
			.imageUrl(question.getImageUrl())
			.build();
	}

	@Override
	@Transactional
	public LeveltestQuestionResponseDTO deleteQuestion(Long questionId) {

		LevelTestQuestion question = leveltestQuestionRepository.findById(questionId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND));

		String lastUrl = question.getImageUrl();
		if (question.getImageKey() != null) {
			s3FileService.delete(question.getImageKey());
		}
		leveltestQuestionRepository.delete(question);

		return LeveltestQuestionResponseDTO.builder()
			.questionId(questionId)
			.imageUrl(lastUrl)
			.build();
	}

	@Transactional
	public void applyManualGrading(Long attemptId, LeveltestGradeRequestDTO request) {

		// 1) 시도(Attempt) 유효성
		LevelTestAttempt attempt = leveltestAttemptRepository.findById(attemptId)
			.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.ATTEMPT_NOT_FOUND));

		// 2) 응답별 점수 반영
		for (LeveltestGradeRequestDTO.QuestionGradeDTO dto : request.getQuestionGrades()) {
			if (dto.getResponseId() == null || dto.getScore() == null) {
				throw new LeveltestException(LeveltestErrorStatus.INVALID_REQUEST);
			}

			LevelTestResponse response = leveltestResponseRepository.findById(dto.getResponseId())
				.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.RESPONSE_NOT_FOUND));

			//  응답이 해당 attempt 소속인지 검증
			if (!response.getLeveltestAttempt().getId().equals(attemptId)) {
				throw new LeveltestException(LeveltestErrorStatus.RESPONSE_NOT_IN_ATTEMPT);
			}

			response.updateScore(dto.getScore());
		}

		// 3) 총점 재계산
		// 3-1) 레포지토리 합계 쿼리 사용
		Integer total = leveltestResponseRepository.sumScoreByAttemptId(attemptId);
		if (total == null)
			total = 0;

		// 4) Attempt에 총점 반영 (엔티티 메서드명은 프로젝트에 맞춰)
		attempt.updateTotalScore(total);

		// 상태 변경
		attempt.markGraded();
	}
}
