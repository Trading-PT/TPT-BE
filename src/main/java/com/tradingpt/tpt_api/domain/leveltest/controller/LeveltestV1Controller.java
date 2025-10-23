package com.tradingpt.tpt_api.domain.leveltest.controller;

import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptListResponseDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubmitRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionUserResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptSubmitResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.service.command.LeveltestCommandService;
import com.tradingpt.tpt_api.domain.leveltest.service.query.LeveltestQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/leveltests")
@RequiredArgsConstructor
@Tag(name = "유저 레벨테스트(Leveltest)", description = "레벨테스트 시도 API")
public class LeveltestV1Controller {

	private final LeveltestCommandService commandService;
	private final LeveltestQueryService queryService;

	@Operation(
		summary = "문제 전체 조회(무한스크롤)",
		description = "DB에 있는 모든 문제를 무한 스크롤로 반환합니다. 예) ?page=0&size=10&sort=id,asc"
	)
	@GetMapping
	public ResponseEntity<BaseResponse<Slice<LevelTestQuestionUserResponseDTO>>> getQuestions(
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Slice<LevelTestQuestionUserResponseDTO> slice = queryService.getQuestions(pageable);
		return ResponseEntity.ok(BaseResponse.onSuccess(slice));
	}

	@Operation(
		summary = "레벨테스트 제출",
		description = "유저가 시험 종료 시 한 번에 모든 응답을 제출합니다. 객관식은 즉시 채점되고, 주관식/단답형은 미채점 상태로 저장됩니다."
	)
	@PostMapping("/attempts")
	public ResponseEntity<BaseResponse<LeveltestAttemptSubmitResponseDTO>> submitLeveltest(
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Valid @RequestBody LeveltestSubmitRequestDTO request
	) {
		LeveltestAttemptSubmitResponseDTO dto = commandService.submitAttempt(customerId, request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(BaseResponse.onSuccessCreate(dto));
	}

	    @Operation(
	            summary = "채점 완료된 시도 조회",
	            description = "status = GRADED 인 시도만 반환합니다."
	    )
	    @GetMapping("/attempts/graded")
	    public ResponseEntity<BaseResponse<List<LeveltestAttemptListResponseDTO>>> getGradedAttempts(
	            @AuthenticationPrincipal(expression = "id") Long customerId
	    ) {
	        List<LeveltestAttemptListResponseDTO> list = queryService.getGradedAttempts(customerId);
	        return ResponseEntity.ok(BaseResponse.onSuccess(list));
	    }

	    @Operation(
	            summary = "시도 상세 조회",
	            description = "특정 attemptId에 해당하는 문제별 응답, 점수, 채점 상태를 반환합니다."
	    )
	    @GetMapping("/attempts/{attemptId}")
	    public ResponseEntity<BaseResponse<LeveltestAttemptDetailResponseDTO>> getAttemptDetail(
	            @PathVariable Long attemptId
	    ) {
	        LeveltestAttemptDetailResponseDTO dto = queryService.getAttemptDetail(attemptId);
	        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	    }
}
