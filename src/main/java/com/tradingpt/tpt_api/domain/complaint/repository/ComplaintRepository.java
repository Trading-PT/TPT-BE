package com.tradingpt.tpt_api.domain.complaint.repository;

import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, ComplaintRepositoryCustom {
    List<Complaint> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM Complaint c
        WHERE c.customer.id = :customerId
        """)
    void deleteByCustomerId(@Param("customerId") Long customerId);
}