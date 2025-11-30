package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachmentDownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureAttachmentDownloadHistoryRepository extends
        JpaRepository<LectureAttachmentDownloadHistory, Long> {

    boolean existsByCustomerIdAndLectureAttachment_Lecture_Id(Long customerId, Long lectureId);
}
