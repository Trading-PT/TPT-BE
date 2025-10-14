package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.response.TrainerListResponseDTO;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepository;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepositoryCustom.AdminRow;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepositoryCustom.AssignedCustomerInfo;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepositoryCustom;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepositoryCustom.TrainerRow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

        // 1) 트레이너/어드민 요약행 조회 (각 Impl에서 QueryDSL로 구현한 것 호출)
        List<TrainerRow> tRows = trainerRepository.findTrainerListRows();
        List<AdminRow> aRows = adminRepository.findAdminListRows();

        // 2) 고객 매핑을 위한 id set
        Set<Long> trainerIds = tRows.stream().map(TrainerRepositoryCustom.TrainerRow::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> adminIds = aRows.stream()
                .map(AdminRow::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3) 배정 고객 맵 (트레이너 / 어드민 각각)
        Map<Long, List<AssignedCustomerInfo>> trainerAssignedMap =
                customerRepository.findAssignedMapByTrainerIds(trainerIds);

        Map<Long, List<AssignedCustomerInfo>> adminAssignedMap =
                customerRepository.findAssignedMapByAdminIds(adminIds);

        List<TrainerListResponseDTO> adminDtos = aRows.stream()
                .map(r -> TrainerListResponseDTO.builder()
                        .trainerId(r.getId())
                        .name(r.getName())
                        .username(r.getUsername())
                        .phone(r.getPhone())
                        .onelineIntroduction(r.getOnelineIntroduction())
                        .profileImageUrl(r.getProfileImageUrl())
                        .role("ROLE_ADMIN")
                        .assignedCustomers(
                                adminAssignedMap.getOrDefault(r.getId(), List.of()).stream()
                                        .map(b -> TrainerListResponseDTO.AssignedCustomerDTO.builder()
                                                .customerId(b.getCustomerId())
                                                .name(b.getName())
                                                .build())
                                        .toList()
                        )
                        .build())
                .toList();


        // 5) 트레이너 DTO (이름 오름차순 정렬)
        List<TrainerListResponseDTO> trainerDtos = tRows.stream()
                .map(r -> TrainerListResponseDTO.builder()
                        .trainerId(r.getId())
                        .name(r.getName())
                        .username(r.getUsername())
                        .phone(r.getPhone())
                        .onelineIntroduction(r.getOnelineIntroduction())
                        .profileImageUrl(r.getProfileImageUrl())
                        .role("ROLE_TRAINER")
                        .assignedCustomers(
                                trainerAssignedMap.getOrDefault(r.getId(), List.of()).stream()
                                        .map(b -> TrainerListResponseDTO.AssignedCustomerDTO.builder()
                                                .customerId(b.getCustomerId())
                                                .name(b.getName())
                                                .build())
                                        .toList()
                        )
                        .build())
                .sorted(Comparator.comparing(TrainerListResponseDTO::getName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        // 6) 어드민 먼저, 그 다음 트레이너(이름순)
        List<TrainerListResponseDTO> result = new ArrayList<>(adminDtos.size() + trainerDtos.size());
        result.addAll(adminDtos);
        result.addAll(trainerDtos);
        return result;
    }


}
