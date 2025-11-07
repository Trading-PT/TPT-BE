package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Page<Lecture> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Lecture> findByLectureExposureOrderByCreatedAtDesc(LectureExposure exposure, Pageable pageable);
}
