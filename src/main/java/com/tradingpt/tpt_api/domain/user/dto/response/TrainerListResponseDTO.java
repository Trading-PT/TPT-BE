package com.tradingpt.tpt_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "트레이너 목록 DTO")
public class TrainerListResponseDTO {

    @Schema(description = "트레이너 id")
    private Long trainerId;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "성함")
    private String name;

    @Schema(description = "전화번호")
    private String phone;

    @Schema(description = "한줄소개")
    private String onelineIntroduction;

    @Schema(description = "로그인 아이디(username)")
    private String username;

    @Schema(description = "트레이너 권한")
    private String role; // "ROLE_TRAINER" | "ROLE_ADMIN"
}
