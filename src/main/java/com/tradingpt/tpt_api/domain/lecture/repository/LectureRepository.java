package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Page<Lecture> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Lecture> findByLectureExposureOrderByCreatedAtDesc(LectureExposure exposure, Pageable pageable);

    /** 전체 강의를 챕터 순서 + 강의 순서 기준으로 정렬해서 가져오기 */
    @Query("""
        SELECT l FROM Lecture l
        JOIN FETCH l.chapter c
        ORDER BY c.chapterOrder ASC, l.lectureOrder ASC
    """)
    List<Lecture> findAllOrderByChapterAndLectureOrder();
}
