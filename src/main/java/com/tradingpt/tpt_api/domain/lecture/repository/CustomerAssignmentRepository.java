package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAssignmentRepository extends JpaRepository<CustomerAssignment, Long> {
    Optional<CustomerAssignment> findByLectureIdAndCustomerId(Long lectureId, Long customerId);
}
