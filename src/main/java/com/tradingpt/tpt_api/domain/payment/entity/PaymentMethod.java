package com.tradingpt.tpt_api.domain.payment.entity;

import com.tradingpt.tpt_api.domain.payment.enums.CardType;
import com.tradingpt.tpt_api.domain.payment.enums.PgProvider;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentMethodType;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "payment_method")
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false)     // TOSSPAYMENTS / KAKAOPAY / NAVERPAY ...
    private PgProvider pgProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)  // CARD / TOSSPAY / KAKAOPAY
    private PaymentMethodType paymentMethod;

    @Column(name = "pg_customer_key", length = 255)
    private String pgCustomerKey;

    @Column(name = "billing_key", length = 255)
    private String billingKey;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "masked_identifier", length = 100)
    private String maskedIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")                         // CREDIT / DEBIT / null(간편결제)
    private CardType cardType;

    @Column(name = "issuer_name", length = 100)
    private String issuerName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "pg_metadata", columnDefinition = "json")
    private String pgMetadata; // JSON은 문자열로 보관(혹은 @Convert)

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
