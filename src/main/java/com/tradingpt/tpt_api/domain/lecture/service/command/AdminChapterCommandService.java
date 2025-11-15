package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.dto.request.ChapterCreateRequestDTO;

public interface AdminChapterCommandService {
    Long createChapter(ChapterCreateRequestDTO req);

    void deleteChapter(Long chapterId);
}
