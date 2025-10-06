package com.tradingpt.tpt_api.domain.complaint.service.command;

import com.tradingpt.tpt_api.domain.complaint.dto.request.CreateComplaintRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.CreateComplaintResponseDTO;

public interface ComplaintCommandService {
    CreateComplaintResponseDTO createComplaint(Long userId, CreateComplaintRequestDTO req);
}
