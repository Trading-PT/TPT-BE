package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.dto.request.LectureRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.Chapter;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.ChapterRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLectureCommandServiceImpl implements AdminLectureCommandService {

    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;     // 업로더(트레이너) 조회용
    private final S3FileService s3FileService;       // S3 삭제용

    /**
     * 강의 생성
     */
    @Override
    @Transactional
    public Long createLecture(LectureRequestDTO req, Long trainerId) {
        // 1. 챕터 조회
        Chapter chapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 2. 업로더(트레이너) 조회
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

        // 3. Lecture 생성 (URL + KEY 둘 다 맵핑)
        Lecture lecture = Lecture.builder()
                .chapter(chapter)
                .trainer(trainer)
                .title(req.getTitle())
                .content(req.getContent())
                .videoUrl(req.getVideoUrl())
                .videoKey(req.getVideoKey())
                .durationSeconds(req.getDurationSeconds() != null ? req.getDurationSeconds() : 0)
                .lectureOrder(req.getLectureOrder())
                .lectureExposure(req.getLectureExposure())
                .attachments(new ArrayList<>())
                .build();

        // 4. 첨부파일 리스트 매핑
        if (req.getAttachments() != null) {
            req.getAttachments().forEach(attReq -> {
                LectureAttachment att = LectureAttachment.builder()
                        .lecture(lecture)
                        .fileUrl(attReq.getFileUrl())
                        .fileKey(attReq.getFileKey())
                        .build();
                lecture.getAttachments().add(att);
            });
        }

        Lecture saved = lectureRepository.save(lecture);
        return saved.getId();
    }

    /**
     * 강의 삭제
     */
    @Override
    @Transactional
    public void deleteLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 1) 강의에 연결된 S3 비디오 삭제
        if (lecture.getVideoKey() != null) {
            s3FileService.delete(lecture.getVideoKey());
        }

        // 2) lecture_attachment 테이블에 있는 첨부파일들도 S3에서 삭제
        if (lecture.getAttachments() != null) {
            lecture.getAttachments().forEach(att -> {
                if (att.getFileKey() != null) {
                    s3FileService.delete(att.getFileKey());
                }
            });
        }

        // 3) 마지막으로 DB에서 강의 삭제
        lectureRepository.delete(lecture);
    }

    /**
     * 강의 수정 (전체 필드 교체, 단 첨부는 바뀐 경우에만 S3 삭제)
     */
    @Override
    @Transactional
    public Long updateLecture(Long lectureId, LectureRequestDTO req, Long trainerId) {
        // 1. 기존 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 2. 변경할 챕터 조회
        Chapter newChapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 3. 수정자(트레이너) 조회
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

        // 4. 비디오가 교체되었으면 기존 S3 삭제
        String oldVideoKey = lecture.getVideoKey();
        String newVideoKey = req.getVideoKey();
        if (oldVideoKey != null && newVideoKey != null && !oldVideoKey.equals(newVideoKey)) {
            s3FileService.delete(oldVideoKey);
        }

        // 5. 첨부파일 변경 여부 판단
        List<LectureAttachment> oldAttachments = lecture.getAttachments();
        var newAttachments = req.getAttachments();

        boolean attachmentsChanged = false;

        if ((oldAttachments == null || oldAttachments.isEmpty()) &&
                (newAttachments != null && !newAttachments.isEmpty())) {
            attachmentsChanged = true;
        } else if ((newAttachments == null || newAttachments.isEmpty()) &&
                (oldAttachments != null && !oldAttachments.isEmpty())) {
            attachmentsChanged = true;
        } else if (oldAttachments != null && newAttachments != null) {
            if (oldAttachments.size() != newAttachments.size()) {
                attachmentsChanged = true;
            } else {
                for (int i = 0; i < oldAttachments.size(); i++) {
                    String oldKey = oldAttachments.get(i).getFileKey();
                    String reqKey = newAttachments.get(i).getFileKey();
                    if (oldKey == null && reqKey == null) {
                        continue;
                    }
                    if (oldKey == null || reqKey == null || !oldKey.equals(reqKey)) {
                        attachmentsChanged = true;
                        break;
                    }
                }
            }
        }

        if (attachmentsChanged) {
            // 기존 것들 S3에서 삭제
            if (oldAttachments != null) {
                oldAttachments.forEach(att -> {
                    if (att.getFileKey() != null) {
                        s3FileService.delete(att.getFileKey());
                    }
                });
                oldAttachments.clear();
            }

            // 새로 들어온 첨부들 다시 추가
            if (newAttachments != null) {
                newAttachments.forEach(attReq -> {
                    LectureAttachment att = LectureAttachment.builder()
                            .lecture(lecture)
                            .fileUrl(attReq.getFileUrl())
                            .fileKey(attReq.getFileKey())
                            .build();
                    lecture.getAttachments().add(att);
                });
            }
        }

        // 6. 나머지 필드 한 방에 업데이트
        lecture.update(
                newChapter,
                trainer,
                req.getTitle(),
                req.getContent(),
                req.getVideoUrl(),
                req.getVideoKey(),
                req.getDurationSeconds(),
                req.getLectureOrder(),
                req.getLectureExposure()
        );

        return lecture.getId();
    }
}
