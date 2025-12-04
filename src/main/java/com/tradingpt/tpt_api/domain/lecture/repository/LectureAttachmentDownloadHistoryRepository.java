package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachmentDownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureAttachmentDownloadHistoryRepository extends
        JpaRepository<LectureAttachmentDownloadHistory, Long> {

    boolean existsByCustomerIdAndLectureAttachment_Lecture_Id(Long customerId, Long lectureId);

    @Modifying
    @Query("""
        delete from LectureAttachmentDownloadHistory dh
        where dh.lectureAttachment.id in (
            select la.id from LectureAttachment la
            where la.lecture.id = :lectureId
        )
        """)
    void deleteByLectureId(@Param("lectureId") Long lectureId);

}
