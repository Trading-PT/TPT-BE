package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureRepository extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {

    Page<Lecture> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Lecture> findByLectureExposureOrderByCreatedAtDesc(LectureExposure exposure, Pageable pageable);

    /** 전체 강의를 챕터 순서 + 강의 순서 기준으로 정렬해서 가져오기 */
    @Query("""
        SELECT l FROM Lecture l
        JOIN FETCH l.chapter c
        ORDER BY c.chapterOrder ASC, l.lectureOrder ASC
    """)
    List<Lecture> findAllOrderByChapterAndLectureOrder();

    @Query("select distinct l from Lecture l " +
            "left join fetch l.attachments " +
            "where l.id = :id")
    Optional<Lecture> findByIdWithAttachments(@Param("id") Long id);

    @Query("""
    SELECT COUNT(l)
    FROM Lecture l
    WHERE l.chapter.chapterType = com.tradingpt.tpt_api.domain.lecture.enums.ChapterType.PRO
    """)
    int countProLectures();

   Optional<Lecture> findByChapter_ChapterTypeAndLectureOrder(ChapterType chapterType, int lectureOrder);

    int countByChapter_ChapterType(ChapterType chapterType);
}
