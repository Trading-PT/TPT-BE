package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.dto.response.CustomerHomeworkSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentAttachmentRepository extends JpaRepository<AssignmentAttachment, Long> {
    List<AssignmentAttachment> findAllByCustomerAssignmentIdOrderByAttemptNoDesc(Long customerAssignmentId);
    int countByCustomerAssignmentId(Long customerAssignmentId);
    List<AssignmentAttachment> findAllByCustomerAssignmentIdOrderByAttemptNoAsc(Long id);
}

