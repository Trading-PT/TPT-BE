package com.tradingpt.tpt_api.domain.user.dto.request;

import com.tradingpt.tpt_api.domain.user.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // @ModelAttribute 를 사용하려면 Setter가 필요함.
@NoArgsConstructor
@Schema(description = "트레이너 등록 요청 DTO")
public class TrainerRequestDTO {

    @Schema(description = "트레이너 성명", example = "김트레이너")
    @NotBlank(message = "트레이너 이름은 필수입니다.")
    private String name;

    @Schema(description = "전화번호(숫자만)", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
    private String phone;

    @Schema(description = "로그인 ID", example = "trainer_lee")
    @NotBlank(message = "로그인 ID는 필수입니다.")
    private String username;

    @Schema(description = "PW", example = "S3cure!Pass")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @Schema(description = "PW 확인", example = "S3cure!Pass")
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordCheck;

    @Schema(description = "한줄소개(선택)", example = "스켈핑 전문 트레이너")
    private String onelineIntroduction;

    @Schema(description = "부여할 권한(ROLE_ADMIN 또는 ROLE_TRAINER)")
    @NotNull(message = "권한은 필수입니다.")
    private Role grantRole;
}
