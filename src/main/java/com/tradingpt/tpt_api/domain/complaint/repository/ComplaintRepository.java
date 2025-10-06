package com.tradingpt.tpt_api.domain.complaint.repository;

import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, ComplaintRepositoryCustom {
    List<Complaint> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

}