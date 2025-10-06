package com.tradingpt.tpt_api.domain.complaint.service.query;

import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import com.tradingpt.tpt_api.domain.complaint.exception.ComplaintErrorStatus;
import com.tradingpt.tpt_api.domain.complaint.exception.ComplaintException;
import com.tradingpt.tpt_api.domain.complaint.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminComplaintQueryServiceImpl implements AdminComplaintQueryService {

    private final ComplaintRepository complaintRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminComplaintResponseDTO> getComplaints(String status, Pageable pageable) {
        return complaintRepository.findAllWithStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponseDTO getComplaintById(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintException(ComplaintErrorStatus.COMPLAINT_NOT_FOUND));
        return ComplaintResponseDTO.from(complaint);
    }

}
