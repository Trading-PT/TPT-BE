package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long>{

    @Query("SELECT COUNT(lp) FROM LectureProgress lp WHERE lp.customer = :customerId")
    int countByCustomerId(Long customerId);

    /** 특정 강의가 이미 열려 있는지 확인 */
    boolean existsByLectureIdAndCustomerId(Long lectureId, Long customerId);

    List<LectureProgress> findByCustomerId(Long customerId);

    Optional<LectureProgress> findByLectureIdAndCustomerId(Long lectureId, Long customerId);
}
