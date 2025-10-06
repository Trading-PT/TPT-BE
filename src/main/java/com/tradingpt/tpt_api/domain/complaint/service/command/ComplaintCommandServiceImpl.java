package com.tradingpt.tpt_api.domain.complaint.service.command;

import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetailsService;
import com.tradingpt.tpt_api.domain.complaint.dto.request.CreateComplaintRequestDTO;
import com.tradingpt.tpt_api.domain.complaint.dto.response.CreateComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.complaint.entity.Complaint;
import com.tradingpt.tpt_api.domain.complaint.repository.ComplaintRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComplaintCommandServiceImpl implements ComplaintCommandService {

    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CreateComplaintResponseDTO createComplaint(Long userId, CreateComplaintRequestDTO req) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

        Complaint c = Complaint.builder()
                .customer(customer)
                .title(req.getTitle())
                .content(req.getContent())
                .build();

        Complaint saved = complaintRepository.save(c);

        return CreateComplaintResponseDTO.builder()
                .id(saved.getId())
                .build();
    }
}
