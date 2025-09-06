package com.tradingpt.tpt_api.user.entity;

import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                // 같은 고객이 같은 UID를 중복 등록하는 것을 방지
                @UniqueConstraint(name = "uk_customer_uid", columnNames = {"customer_id", "uid"})
        }
)
@Getter
@Builder
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Uid extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String exchangeName;  // 거래소명 (필요 시 Enum로 승격 가능)

    @Column(nullable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

}
