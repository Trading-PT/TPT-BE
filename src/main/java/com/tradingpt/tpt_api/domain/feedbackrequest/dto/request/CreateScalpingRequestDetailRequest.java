package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "스켈핑 피드백 요청 DTO")
public class CreateScalpingRequestDetailRequest {

    @NotNull
    @Schema(description = "요청 날짜")
    private LocalDate requestDate;

    @NotBlank
    @Schema(description = "종목")
    private String category;

    @Min(1) @Max(1000)
    @Schema(description = "하루 매매 횟수")
    private Integer dailyTradingCount;

    @Schema(description = "스크린샷 이미지 파일")
    private List<MultipartFile> screenshotFiles;

    @Schema(description = "스크린샷 이미지 URL")
    private String screenshotImageUrl;

    @Min(1) @Max(100)
    @Schema(description = "리스크 테이킹 (1-100)")
    private Integer riskTaking;

    @Min(1) @Max(1000)
    @Schema(description = "레버리지 (1-1000)")
    private Integer leverage;

    @Min(1) @Max(1000)
    @Schema(description = "총 포지션 잡은 횟수")
    private Integer totalPositionTakingCount;

    @Min(0) @Max(1000)
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

    @Schema(description = "완강 여부")
    private Boolean isCourseCompleted;

    @Schema(description = "피드백 요청 연도")
    private Integer feedbackYear;

    @Schema(description = "피드백 요청 월")
    private Integer feedbackMonth;

    @Schema(description = "피드백 요청 주차")
    private Integer feedbackWeek;
}