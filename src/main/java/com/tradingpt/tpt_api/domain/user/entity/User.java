package com.tradingpt.tpt_api.domain.user.entity;


import com.tradingpt.tpt_api.domain.user.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder가 사용
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username; //로컬일 경우 id, 소셜일경우 KAKAO_3430803880

    @Column(name = "email")
    private String email;

    private String password; //소셜일경우 null

    @Column(name = "name", nullable = false)
    private String name; //유저의 이름

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;
}

