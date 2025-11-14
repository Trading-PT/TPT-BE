package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import java.util.List;

public interface LectureRepositoryCustom {

    List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size);
}
