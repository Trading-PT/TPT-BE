package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="customer")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Provider provider;      // LOCAL, KAKAO, NAVER

    @Column(name="provider_id")
    private String providerId; // 소셜 id

    @Column(name="phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="primary_investment_type")
    private InvestmentType primaryInvestmentType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name="membership_level")
    private MembershipLevel membershipLevel;

    @Column(name="membership_expired_at")
    private LocalDateTime membershipExpiredAt;

    @Column(name="is_course_completed")
    private Boolean isCourseCompleted = Boolean.FALSE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Uid> uids = new ArrayList<>();

    public void addUid(Uid u) {
        u.setCustomer(this);
        this.uids.add(u);
    }
}
