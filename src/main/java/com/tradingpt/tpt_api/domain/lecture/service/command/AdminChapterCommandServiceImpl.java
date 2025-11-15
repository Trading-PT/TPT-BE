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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminChapterCommandServiceImpl implements AdminChapterCommandService {

    private final ChapterRepository chapterRepository;

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

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        chapterRepository.deleteById(chapterId);
    }
}
