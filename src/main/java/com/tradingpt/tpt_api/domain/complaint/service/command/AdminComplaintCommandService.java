// AdminComplaintCommandService.java
package com.tradingpt.tpt_api.domain.complaint.service.command;

import com.tradingpt.tpt_api.domain.complaint.dto.request.AdminReplyRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;

public interface AdminComplaintCommandService {

    /** 답변 등록/수정(업서트): answered_id/answered_at/status 까지 함께 갱신 */
    ComplaintResponseDTO upsertReply(Long complaintId, Long answeredUserId, AdminReplyRequestDTO dto);

    /** 답변 삭제: reply/answered_id/answered_at 초기화 + status=UNANSWERED */
    void deleteReply(Long complaintId);
}
