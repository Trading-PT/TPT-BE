package com.tradingpt.tpt_api.domain.auth.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Admin;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMeResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String nickname;
    private String profileImageUrl;

    private String phoneNumber;
    private String oneLineIntroduction;

    public static AdminMeResponse from(User u, Trainer t) {
        return AdminMeResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .phoneNumber(t != null ? t.getPhoneNumber() : null)
                .oneLineIntroduction(t != null ? t.getOnelineIntroduction() : null)
                .build();
    }

    public static AdminMeResponse from(User u, Admin a) {
        return AdminMeResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .phoneNumber(a != null ? a.getPhoneNumber() : null)
                .oneLineIntroduction(a != null ? a.getOnelineIntroduction() : null)
                .build();
    }


}

