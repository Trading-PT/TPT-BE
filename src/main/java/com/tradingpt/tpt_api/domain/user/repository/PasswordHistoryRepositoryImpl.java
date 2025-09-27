package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.user.entity.QPasswordHistory.passwordHistory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasswordHistoryRepositoryImpl  implements PasswordHistoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findRecentHashesByUserId(Long userId, int limit) {
        return queryFactory
                .select(passwordHistory.passwordHash)
                .from(passwordHistory)
                .where(passwordHistory.user.id.eq(userId))
                .orderBy(passwordHistory.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long deleteOlderThanNByUserId(Long userId, int n) {
        // n개 이후(OFFSET n) id만 뽑아서 일괄 삭제
        List<Long> idsToDelete = queryFactory
                .select(passwordHistory.id)
                .from(passwordHistory)
                .where(passwordHistory.user.id.eq(userId))
                .orderBy(passwordHistory.createdAt.desc())
                .offset(n)
                .fetch();

        if (idsToDelete.isEmpty()) return 0L;

        return queryFactory
                .delete(passwordHistory)
                .where(passwordHistory.id.in(idsToDelete))
                .execute();
    }
}
