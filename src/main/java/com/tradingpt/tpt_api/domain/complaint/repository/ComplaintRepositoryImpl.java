package com.tradingpt.tpt_api.domain.complaint.repository;

import static com.tradingpt.tpt_api.domain.complaint.entity.QComplaint.complaint;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.customer;
import static com.tradingpt.tpt_api.domain.user.entity.QTrainer.trainer;
import static com.tradingpt.tpt_api.domain.user.entity.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.complaint.dto.response.AdminComplaintResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.QTrainer;
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

        // 답변 여부 판단(간단 규칙: reply 또는 answeredAt 이 있으면 답변됨)
        BooleanExpression answeredCond =
                complaint.complaintReply.isNotNull().or(complaint.answeredAt.isNotNull());

        BooleanExpression where = switch (s) {
            case "ANSWERED"   -> answeredCond;
            case "UNANSWERED" -> answeredCond.not();
            default           -> null; // ALL
        };

        // 답변 작성자(트레이너) 별칭
        var answeredTrainer = new QTrainer("ansTrainer");

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

        List<AdminComplaintResponseDTO> content = q
                .select(Projections.constructor(
                        AdminComplaintResponseDTO.class,
                        complaint.id,
                        customer.name,                // 성함
                        customer.phoneNumber,         // 전화번호
                        trainer.name,       // 담당 조교
                        complaint.title,              // 제목
                        answeredCond,                 // answered 여부
                        answeredTrainer.name,  // 답변 작성자(트레이너명)
                        complaint.answeredAt,         // 답변 시각
                        complaint.createdAt           // 등록 시각
                ))
                .from(complaint)
                .join(complaint.customer, customer)          // 고객
                .leftJoin(customer.assignedTrainer, user)         // 고객 담당 조교
                .leftJoin(complaint.answeredBy, answeredTrainer) // 답변자(트레이너)
                .where(where)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = q.select(complaint.count())
                .from(complaint)
                .join(complaint.customer, customer)
                .leftJoin(customer.assignedTrainer, user)
                .leftJoin(complaint.answeredBy, answeredTrainer)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
