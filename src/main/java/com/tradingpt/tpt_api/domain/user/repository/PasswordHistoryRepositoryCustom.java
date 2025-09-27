package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;

public interface PasswordHistoryRepositoryCustom {

    List<String> findRecentHashesByUserId(Long userId, int limit);

    /** 최근 n개를 제외한 나머지 삭제 */
    long deleteOlderThanNByUserId(Long userId, int n);
}
