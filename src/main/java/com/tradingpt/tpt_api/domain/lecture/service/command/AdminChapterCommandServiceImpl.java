package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.dto.request.ChapterCreateRequestDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.Chapter;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminChapterCommandServiceImpl implements AdminChapterCommandService {

    private final ChapterRepository chapterRepository;

    @Override
    public Long createChapter(ChapterCreateRequestDTO req) {
        Chapter chapter = Chapter.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .build();
        Chapter saved = chapterRepository.save(chapter);
        return saved.getId();
    }

    @Override
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        chapterRepository.deleteById(chapterId);
    }
}
