package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long>{

    @Query("SELECT COUNT(lp) FROM LectureProgress lp WHERE lp.customer = :customerId")
    int countByCustomerId(Long customerId);

    /** 특정 강의가 이미 열려 있는지 확인 */
    boolean existsByLectureIdAndCustomerId(Long lectureId, Long customerId);

    List<LectureProgress> findByCustomerId(Long customerId);

    Optional<LectureProgress> findByLecture_IdAndCustomer_Id(Long lectureId, Long customerId);

    @Query("""
        SELECT COUNT(lp)
        FROM LectureProgress lp
        WHERE lp.customer.id = :customerId
          AND lp.lecture.chapter.chapterType = com.tradingpt.tpt_api.domain.lecture.enums.ChapterType.PRO
          AND lp.isCompleted = true
        """)
    int countCompletedProLectures(@Param("customerId") Long customerId);

    boolean existsByCustomerAndLecture(Customer customer, Lecture lecture);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from LectureProgress lp where lp.lecture.id = :lectureId")
    void deleteByLectureId(@Param("lectureId") Long lectureId);

    // S3 삭제 + 존재 여부 확인용
    @Query("""
        select l from Lecture l
        left join fetch l.chapter
        where l.id = :lectureId
        """)
    Optional<Lecture> findByIdForDelete(@Param("lectureId") Long lectureId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM LectureProgress lp
        WHERE lp.customer.id = :customerId
        """)
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
