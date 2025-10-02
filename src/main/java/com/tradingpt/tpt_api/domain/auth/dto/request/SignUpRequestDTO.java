package com.tradingpt.tpt_api.domain.auth.dto.request;

import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

@Getter
@Builder
public class SignUpRequestDTO {

    @Schema(description = "실명(또는 서비스 표시명으로 사용 가능)", example = "홍길동")
    @NotBlank
    private String name;

    @Schema(description = "휴대폰 번호(숫자/하이픈 허용)", example = "010-1234-5678")
    @NotBlank
    private String phone;

    @Schema(description = "이메일 주소", example = "user@example.com")
    @Email @NotBlank
    private String email;

    @Schema(description = "로그인용 ID(계정 식별자)", example = "gildong123")
    @NotBlank
    private String username;

    @Schema(description = "로그인 비밀번호(8~64자)", example = "Passw0rd!23")
    @Size(min = 8, max = 64)
    private String password;

    @Schema(description = "비밀번호 확인(동일해야 함)", example = "Passw0rd!23")
    @Size(min = 8, max = 64)
    private String passwordCheck;

    @Schema(description = "필수 약관 동의(필수)", example = "true")

    @NotNull
    private Boolean termsService;

    @Schema(description = "개인정보 수집·이용 동의(필수)", example = "true")
    @NotNull
    private Boolean termsPrivacy;

    @Schema(description = "마케팅 정보 수신 동의(선택)", example = "false")
    private Boolean termsMarketing;

    @Schema(
            description = "나의 투자 유형",
            allowableValues = {"SWING","DAY","SCALPING"},
            example = "DAY"
    )
    @NotNull
    private InvestmentType investmentType;

    @Schema(description = "거래 UID 목록(최대 5개)")
    @Size(max = 5, message = "UID는 최대 5개까지 등록 가능합니다.")
    private List<UidRequest> uids;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UidRequest {
        @Schema(description = "거래소명", example = "BINANCE")
        @NotBlank
        private String exchangeName;

        @Schema(description = "UID 값", example = "upbit-uid-123456")
        @NotBlank
        private String uid;

    }
}

