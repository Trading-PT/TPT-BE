package com.tradingpt.tpt_api.domain.review.repository;

import com.tradingpt.tpt_api.domain.review.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM ReviewAttachment a
        WHERE a.review.customer.id = :customerId
        """)
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
