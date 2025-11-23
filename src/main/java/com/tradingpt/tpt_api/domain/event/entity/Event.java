package com.tradingpt.tpt_api.domain.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "event")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "name")
    private String name;          // 이벤트 이름

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "token_amount", nullable = false)
    private int tokenAmount; // 보상 토큰 개수 (1, 5 등)

    @Column(name = "active", nullable = false)
    private boolean active; // 활성 여부

    public void update(String name,
                       LocalDateTime startAt,
                       LocalDateTime endAt,
                       int tokenAmount,
                       boolean active) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.tokenAmount = tokenAmount;
        this.active = active;
    }
}
