package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureAttachmentRepository extends JpaRepository<LectureAttachment, Long> {

    // S3 삭제용 fileKey만 가져오는 쿼리 (select 1번)
    @Query("""
        select la.fileKey
        from LectureAttachment la
        where la.lecture.id = :lectureId
        """)
    List<String> findFileKeysByLectureId(@Param("lectureId") Long lectureId);

    @Modifying
    @Query("delete from LectureAttachment la where la.lecture.id = :lectureId")
    void deleteByLectureId(@Param("lectureId") Long lectureId);
}
