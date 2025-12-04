package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerAssignmentRepository extends JpaRepository<CustomerAssignment, Long> {
    Optional<CustomerAssignment> findByLectureIdAndCustomerId(Long lectureId, Long customerId);
    @Modifying
    @Query("delete from CustomerAssignment ca where ca.lecture.id = :lectureId")
    void deleteByLectureId(@Param("lectureId") Long lectureId);
}
