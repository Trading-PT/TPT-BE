package com.tradingpt.tpt_api.domain.leveltest.service.command;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubmitRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptSubmitResponseDTO;

public interface LeveltestCommandService {

    LeveltestAttemptSubmitResponseDTO submitAttempt(Long customerId, LeveltestSubmitRequestDTO request);
}
