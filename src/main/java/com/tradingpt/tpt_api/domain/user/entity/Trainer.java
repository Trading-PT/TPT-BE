package com.tradingpt.tpt_api.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="trainer")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Trainer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="profile_image_url")
    private String profileImageUrl;

    @Column(name="oneline_introduction")
    private String oneLineIntroduction;

    @OneToOne
    @JoinColumn(name="user_id", unique = true)
    private User user;
}
