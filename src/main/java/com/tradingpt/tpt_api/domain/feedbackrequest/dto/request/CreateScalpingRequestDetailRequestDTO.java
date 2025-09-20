package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "스켈핑 피드백 요청 DTO")
public class CreateScalpingRequestDetailRequestDTO {

	@Schema(description = "완강 여부")
	private Boolean isCourseCompleted;

	@Schema(description = "요청 날짜")
	private LocalDate requestDate;

	@Schema(description = "종목")
	private String category;

	@Schema(description = "하루 매매 횟수")
	private Integer dailyTradingCount;

	@Schema(description = "스크린샷 이미지 파일")
	private List<MultipartFile> screenshotFiles;

	@Schema(description = "리스크 테이킹 (1-100)")
	private Integer riskTaking;

	@Schema(description = "레버리지 (1-1000)")
	private Integer leverage;

	@Schema(description = "총 포지션 잡은 횟수")
	private Integer totalPositionTakingCount;

	@Schema(description = "총 매매 횟수 대비 수익 매매횟수")
	private Integer totalProfitMarginPerTrades;

	@Schema(description = "스켈핑 시 포지션을 진입하는 근거")
	private String positionStartReason;

	@Schema(description = "스켈핑 시 포지션을 종료하는 근거")
	private String positionEndReason;

	@Schema(description = "담당 트레이너 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

	@Schema(description = "15분봉 기준 추세 분석")
	private String trendAnalysis;

}