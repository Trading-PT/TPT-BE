package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

public interface AdminUserCommandService {
    void updateUserStatus(Long userId, UserStatus newStatus);
}
