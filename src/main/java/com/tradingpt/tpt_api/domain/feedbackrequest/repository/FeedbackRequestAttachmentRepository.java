package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRequestAttachmentRepository extends JpaRepository<FeedbackRequestAttachment, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM FeedbackRequestAttachment a
        WHERE a.feedbackRequest.customer.id = :customerId
        """)
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
