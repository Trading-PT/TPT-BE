package com.tradingpt.tpt_api.domain.complaint.repository;

import static com.tradingpt.tpt_api.domain.complaint.entity.QComplaint.complaint;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.customer;
import static com.tradingpt.tpt_api.domain.user.entity.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.QUser;
import com.tradingpt.tpt_api.domain.user.entity.QTrainer; // 답변 단 사람은 트레이너라면 이건 유지
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ComplaintRepositoryImpl implements ComplaintRepositoryCustom {

    private final JPAQueryFactory q;

    @Override
    public Page<AdminComplaintResponseDTO> findAllWithStatus(String status, Pageable pageable) {
        String s = (status == null) ? "ALL" : status.trim().toUpperCase();

        // 답변됨 조건
        BooleanExpression answeredCond =
                complaint.complaintReply.isNotNull().or(complaint.answeredAt.isNotNull());

        // 상태 필터
        BooleanExpression where = switch (s) {
            case "ANSWERED"   -> answeredCond;
            case "UNANSWERED" -> answeredCond.not();
            default           -> null; // ALL
        };

        // 고객에게 배정된 담당자(조교/어드민 등)를 위한 alias
        QUser assignedUser = new QUser("assignedUser");

        // 답변 남긴 사람(이건 너 코드처럼 트레이너로 유지한 듯해서 그대로 별칭)
        QTrainer answeredTrainer = new QTrainer("ansTrainer");

        // 정렬
        OrderSpecifier<?>[] orders = pageable.getSort().stream()
                .map(order -> {
                    Order dir = order.isAscending() ? Order.ASC : Order.DESC;
                    return switch (order.getProperty()) {
                        case "answeredAt" -> new OrderSpecifier<>(dir, complaint.answeredAt);
                        case "title"      -> new OrderSpecifier<>(dir, complaint.title);
                        default           -> new OrderSpecifier<>(dir, complaint.createdAt);
                    };
                })
                .toArray(OrderSpecifier[]::new);

        // 실제 데이터 조회
        List<AdminComplaintResponseDTO> content = q
                .select(Projections.constructor(
                        AdminComplaintResponseDTO.class,
                        complaint.id,              // 민원 ID
                        customer.name,              // 고객 이름
                        customer.phoneNumber,       // 고객 전화
                        assignedUser.name,          // ✅ 고객에게 배정된 담당자 (user로 조인했으니 user.name)
                        complaint.title,            // 제목
                        answeredCond,               // 답변 여부
                        answeredTrainer.name,       // 답변 작성자
                        complaint.answeredAt,       // 답변 시각
                        complaint.createdAt         // 등록 시각
                ))
                .from(complaint)
                .join(complaint.customer, customer)           // 민원 올린 고객
                .leftJoin(customer.assignedTrainer, assignedUser) // ✅ 고객에게 배정된 user
                .leftJoin(complaint.trainer, answeredTrainer) // 답변 남긴 트레이너
                .where(where)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수
        Long total = q
                .select(complaint.count())
                .from(complaint)
                .join(complaint.customer, customer)
                .leftJoin(customer.assignedTrainer, assignedUser)
                .leftJoin(complaint.trainer, answeredTrainer)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
