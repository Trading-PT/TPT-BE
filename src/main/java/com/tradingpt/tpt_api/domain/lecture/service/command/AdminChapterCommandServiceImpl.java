package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.dto.request.ChapterCreateRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.Chapter;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.ChapterRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminChapterCommandServiceImpl implements AdminChapterCommandService {

    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;              // 👈 추가
    private final AdminLectureCommandService adminLectureCommandService;

    @Override
    @Transactional
    public Long createChapter(ChapterCreateRequestDTO req) {
        Chapter chapter = Chapter.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .chapterType(req.getChapterType())
                .chapterOrder(req.getChapterOrder())
                .build();
        Chapter saved = chapterRepository.save(chapter);
        return saved.getId();
    }

    @Transactional
    public Long updateChapter(Long chapterId, ChapterCreateRequestDTO request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        chapter.update(
                request.getTitle(),
                request.getDescription(),
                request.getChapterOrder(),
                request.getChapterType()
        );

        return chapter.getId();
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {

        // 1) 챕터 존재 여부 체크
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 2) 챕터에 속한 강의들 조회
        List<Lecture> lectures = lectureRepository.findAllByChapter_Id(chapterId);

        // 3) 강의별로 기존 deleteLecture 로 FULL 삭제 (S3, progress, attachments 포함)
        lectures.forEach(lecture ->
                adminLectureCommandService.deleteLecture(lecture.getId())
        );

        // 4) 마지막으로 챕터 삭제
        chapterRepository.delete(chapter);
    }
}
