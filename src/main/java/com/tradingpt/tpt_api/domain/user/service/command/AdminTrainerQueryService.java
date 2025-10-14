package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.response.TrainerListResponseDTO;
import java.util.List;

public interface AdminTrainerQueryService {

    List<TrainerListResponseDTO> getTrainers();
}
