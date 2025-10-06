package com.tradingpt.tpt_api.domain.complaint.service.query;

import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintQueryServiceImpl implements ComplaintQueryService {

    private final ComplaintRepository complaintRepository;

    // 예: ComplaintQueryService (고객용)
    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> getComplaintsByUser(Long userId) {
        // Customer의 PK = user_id 라면 userId 그대로 사용해도 됩니다.
        return complaintRepository.findByCustomerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ComplaintResponseDTO::from)
                .toList();
    }

}
