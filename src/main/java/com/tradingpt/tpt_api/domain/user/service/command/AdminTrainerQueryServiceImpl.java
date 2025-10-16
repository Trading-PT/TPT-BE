package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.response.AssignedCustomerDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerListResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepository;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepositoryCustom.AdminRow;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepositoryCustom.TrainerRow;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminTrainerQueryServiceImpl implements AdminTrainerQueryService{

    private final CustomerRepository customerRepository;
    private final TrainerRepository trainerRepository;
    private final AdminRepository adminRepository;

    @Override
    public List<TrainerListResponseDTO> getTrainers() {
        // 1) 트레이너/어드민 요약행 조회
        List<TrainerRow> tRows = trainerRepository.findTrainerListRows();
        List<AdminRow> aRows = adminRepository.findAdminListRows();

        // 2) 어드민 DTO (배정 고객은 조회하지 않음)
        List<TrainerListResponseDTO> adminDtos = aRows.stream()
                .map(r -> TrainerListResponseDTO.builder()
                        .trainerId(r.getId())
                        .name(r.getName())
                        .username(r.getUsername())
                        .phone(r.getPhone())
                        .onelineIntroduction(r.getOnelineIntroduction())
                        .profileImageUrl(r.getProfileImageUrl())
                        .role("ROLE_ADMIN")
                        .build())
                .toList();

        // 3) 트레이너 DTO (이름 오름차순)
        List<TrainerListResponseDTO> trainerDtos = tRows.stream()
                .map(r -> TrainerListResponseDTO.builder()
                        .trainerId(r.getId())
                        .name(r.getName())
                        .username(r.getUsername())
                        .phone(r.getPhone())
                        .onelineIntroduction(r.getOnelineIntroduction())
                        .profileImageUrl(r.getProfileImageUrl())
                        .role("ROLE_TRAINER")
                        .build())
                .sorted(Comparator.comparing(TrainerListResponseDTO::getName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        // 4) 어드민 먼저, 그 다음 트레이너
        List<TrainerListResponseDTO> result = new ArrayList<>(adminDtos.size() + trainerDtos.size());
        result.addAll(adminDtos);
        result.addAll(trainerDtos);
        return result;
    }


    @Override
    public List<AssignedCustomerDTO> getAssignedCustomers(Long trainerId) {

        // 1. 존재 검증: 실제 트레이너인지 체크
        trainerRepository.findById(trainerId)
                .orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

        // 2. 해당 트레이너에 배정된 고객 조회
        List<Customer> customers = customerRepository.findByAssignedTrainer_Id(trainerId);

        // 3. DTO 매핑
        List<AssignedCustomerDTO> assignedCustomerDTOs = customers.stream()
                .map(customer -> AssignedCustomerDTO.builder()
                        .customerId(customer.getId())
                        .name(customer.getName())
                        .build())
                .toList();

        return assignedCustomerDTOs;
    }
}

