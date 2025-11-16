package com.tradingpt.tpt_api.domain.lecture.service.command;

import org.springframework.web.multipart.MultipartFile;

public interface LectureCommandService {
    Long purchaseLecture(Long lectureId, Long userId);

    void updateLectureProgress(Long userId, Long lectureId, int currentSeconds);

    Long submitAssignment(Long userId, Long lectureId, MultipartFile file);
}

