package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.dto.response.CustomerHomeworkSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentAttachmentRepository extends JpaRepository<AssignmentAttachment, Long> {
    List<AssignmentAttachment> findAllByCustomerAssignmentIdOrderByAttemptNoDesc(Long customerAssignmentId);
    int countByCustomerAssignmentId(Long customerAssignmentId);
    List<AssignmentAttachment> findAllByCustomerAssignmentIdOrderByAttemptNoAsc(Long id);

    @Modifying
    @Query("""
        delete from AssignmentAttachment aa
        where aa.customerAssignment.lecture.id = :lectureId
        """)
    void deleteByLectureId(@Param("lectureId") Long lectureId);
}

