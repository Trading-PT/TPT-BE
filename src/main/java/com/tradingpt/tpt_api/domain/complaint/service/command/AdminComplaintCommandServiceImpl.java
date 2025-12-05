package com.tradingpt.tpt_api.domain.complaint.service.command;

import com.tradingpt.tpt_api.domain.complaint.dto.request.AdminReplyRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.ComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import com.tradingpt.tpt_api.domain.complaint.exception.ComplaintErrorStatus;
import com.tradingpt.tpt_api.domain.complaint.exception.ComplaintException;
import com.tradingpt.tpt_api.domain.complaint.repository.ComplaintRepository;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminComplaintCommandServiceImpl implements AdminComplaintCommandService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    @Override
    public ComplaintResponseDTO upsertReply(Long complaintId, Long answeredUserId, AdminReplyRequestDTO request) {
        // 1) 민원 조회
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintException(ComplaintErrorStatus.COMPLAINT_NOT_FOUND));

        // 2) 답변자(트레이너,어드민) 조회
        User answeredBy = userRepository.findById(answeredUserId)
                .orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

        // 3) 엔티티 도메인 메서드로 상태 일괄 갱신 (ANSWERED/answeredAt/answeredBy/complaintReply)
        complaint.upsertReply(answeredBy, request.getReply(), LocalDateTime.now());

        // 4) DTO 변환 후 반환
        return ComplaintResponseDTO.from(complaint);
    }

    @Override
    public void deleteReply(Long complaintId) {
        // 1) 민원 조회
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintException(ComplaintErrorStatus.COMPLAINT_NOT_FOUND));

        // 2) 답변 삭제(UNANSWERED로 롤백)
        complaint.deleteReply(LocalDateTime.now());
    }
}
