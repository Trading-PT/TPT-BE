package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
import java.util.List;

public interface LectureQueryService {
    List<ChapterBlockDTO> getCurriculum(Long userId, int page, int size);
    LectureDetailDTO getLectureDetail(Long userId, Long lectureId);
    AssignmentSubmissionDetailDTO getMyAssignmentDetail(Long userId, Long lectureId);
    LecturePlayResponseDTO getLecturePlayUrl(Long userId, Long lectureId);
}

