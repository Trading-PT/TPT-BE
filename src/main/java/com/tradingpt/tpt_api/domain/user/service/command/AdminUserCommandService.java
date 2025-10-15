package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.request.GiveUserTokenRequestDTO;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

public interface AdminUserCommandService {

	void updateUserStatus(Long userId, UserStatus newStatus);

	Void giveUserTokens(Long userId, GiveUserTokenRequestDTO request);
}
