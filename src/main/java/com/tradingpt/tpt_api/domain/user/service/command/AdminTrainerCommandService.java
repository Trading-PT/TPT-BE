package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.request.TrainerRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AdminTrainerCommandService {
    TrainerResponseDTO createTrainer(TrainerRequestDTO req, MultipartFile profileImage);

    TrainerResponseDTO updateTrainer(Long trainerId, TrainerRequestDTO req, MultipartFile profileImage);

    void deleteTrainer(Long trainerId);

    Long reassignCustomerToTrainer(Long trainerId, Long customerId);
}
