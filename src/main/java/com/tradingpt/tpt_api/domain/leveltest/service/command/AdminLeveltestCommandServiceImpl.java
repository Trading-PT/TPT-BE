package com.tradingpt.tpt_api.domain.leveltest.service.command;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestMultipleChoiceRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubjectiveRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestQuestionErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestQuestionException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestQuestionRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminLeveltestCommandServiceImpl implements AdminLeveltestCommandService {

    private final LeveltestQuestionRepository leveltestQuestionRepository;
    private final S3FileService s3FileService;

    private static final String LEVELTEST_DIR = "leveltests/";

    @Override
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

        LeveltestQuestion entity = LeveltestQuestion.builder()
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

        LeveltestQuestion saved = leveltestQuestionRepository.save(entity);

        return LeveltestQuestionResponseDTO.builder()
                .questionId(saved.getId())
                .imageUrl(saved.getImageUrl())
                .build();
    }

    @Override
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

        LeveltestQuestion entity = LeveltestQuestion.builder()
                .content(req.getContent())
                .score(req.getScore())
                .problemType(req.getProblemType())
                .imageKey(imageKey)
                .imageUrl(imageUrl)
                .answerText(req.getAnswerText())
                .build();

        LeveltestQuestion saved = leveltestQuestionRepository.save(entity);

        return LeveltestQuestionResponseDTO.builder()
                .questionId(saved.getId())
                .imageUrl(saved.getImageUrl())
                .build();
    }


    @Override
    public LeveltestQuestionResponseDTO updateMultipleChoiceQuestion(
            Long questionId, LeveltestMultipleChoiceRequestDTO req, MultipartFile image
    ) {
        LeveltestQuestion question = leveltestQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LeveltestQuestionException(LeveltestQuestionErrorStatus.QUESTION_NOT_FOUND));


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
        question.changeChoices(req.getChoice1(), req.getChoice2(), req.getChoice3(), req.getChoice4(), req.getChoice5());
        question.changeCorrectChoiceNum(req.getCorrectChoiceNum());

        return LeveltestQuestionResponseDTO.builder()
                .questionId(question.getId())
                .imageUrl(question.getImageUrl())
                .build();
    }

    @Override
    public LeveltestQuestionResponseDTO updateTextAnswerQuestion(
            Long questionId, LeveltestSubjectiveRequestDTO req, MultipartFile image
    ) {

        LeveltestQuestion question = leveltestQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LeveltestQuestionException(LeveltestQuestionErrorStatus.QUESTION_NOT_FOUND));

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
    public LeveltestQuestionResponseDTO deleteQuestion(Long questionId) {

        LeveltestQuestion question = leveltestQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LeveltestQuestionException(LeveltestQuestionErrorStatus.QUESTION_NOT_FOUND));

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
}
