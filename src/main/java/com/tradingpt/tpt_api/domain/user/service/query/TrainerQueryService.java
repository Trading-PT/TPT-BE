package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tradingpt.tpt_api.domain.user.dto.response.CustomerEvaluationResponseDTO;

public interface TrainerQueryService {

	Page<CustomerEvaluationResponseDTO> getManagedCustomersEvaluations(Pageable pageable, Long trainerId);

}
