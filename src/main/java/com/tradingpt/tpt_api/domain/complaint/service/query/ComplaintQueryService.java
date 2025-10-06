package com.tradingpt.tpt_api.domain.complaint.service.query;

import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import java.util.List;

public interface ComplaintQueryService {
    List<ComplaintResponseDTO> getComplaintsByUser(Long userId);
}
