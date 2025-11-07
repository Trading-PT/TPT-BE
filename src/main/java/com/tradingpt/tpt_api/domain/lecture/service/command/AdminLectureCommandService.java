package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.dto.request.LectureRequestDTO;

public interface AdminLectureCommandService {
    Long createLecture(LectureRequestDTO request, Long trainerId);
    Long updateLecture(Long lectureId, LectureRequestDTO req, Long trainerId);
    void deleteLecture(Long lectureId);
}

