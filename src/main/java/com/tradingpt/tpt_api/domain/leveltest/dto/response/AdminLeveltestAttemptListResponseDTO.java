package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 목록 조회 DTO")
public class AdminLeveltestAttemptListResponseDTO {

	@Schema(description = "시도(Attempt) ID", example = "15")
	private Long attemptId;

	@Schema(description = "유저 이름", example = "홍길동")
	private String customerName;

	@Schema(description = "레벨테스트 상태", example = "GRADING")
	private LevelTestStaus status;

	@Schema(description = "시도 생성일시", example = "2025-10-19T13:40:21")
	private LocalDateTime createdAt;
}
