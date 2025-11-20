package com.tradingpt.tpt_api.domain.leveltest.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestGradeRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestMultipleChoiceRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubjectiveRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.AdminLeveltestAttemptListResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LevelTestQuestionDetailResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestQuestionResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.service.command.AdminLeveltestCommandService;
import com.tradingpt.tpt_api.domain.leveltest.service.query.AdminLeveltestQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/admin/leveltests")
@RequiredArgsConstructor
@Tag(name = "관리자(Admin) - 레벨테스트(Leveltest) 관리", description = "레벨테스트 문제 관리 API")
public class AdminLeveltestV1Controller {

	private final AdminLeveltestCommandService commandService;
	private final AdminLeveltestQueryService queryService;

	@Operation(summary = "객관식 문제 생성(multipart)")
	@PostMapping(value = "/multiple-choice", consumes = "multipart/form-data")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> createMultipleChoice(
		@Valid @ModelAttribute LeveltestMultipleChoiceRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.createMultipleChoiceQuestion(request, image);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(dto));
	}

	@Operation(summary = "단답형 문제 생성(multipart)")
	@PostMapping(value = "/short-answer", consumes = "multipart/form-data")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> createShortAnswer(
		@Valid @ModelAttribute LeveltestSubjectiveRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.createTextAnswerQuestion(request, image);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(dto));
	}

	@Operation(summary = "서술형 문제 생성(multipart)")
	@PostMapping(value = "/subjective", consumes = "multipart/form-data")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> createSubjective(
		@Valid @ModelAttribute LeveltestSubjectiveRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.createTextAnswerQuestion(request, image);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(dto));
	}

	@Operation(summary = "객관식 문제 수정(multipart)", description = "모든 필드 + 이미지 교체 가능")
	@PutMapping(value = "/multiple-choice/{questionId}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> updateMultipleChoice(
		@PathVariable Long questionId,
		@Valid @ModelAttribute LeveltestMultipleChoiceRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.updateMultipleChoiceQuestion(questionId, request, image);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(summary = "단답형 문제 수정(multipart)", description = "모든 필드 + 이미지 교체 가능")
	@PutMapping(value = "/short-answer/{questionId}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> updateShortAnswer(
		@PathVariable Long questionId,
		@Valid @ModelAttribute LeveltestSubjectiveRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.updateTextAnswerQuestion(questionId, request, image);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(summary = "서술형 문제 수정(multipart)", description = "모든 필드 + 이미지 교체 가능")
	@PutMapping(value = "/subjective/{questionId}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<LeveltestQuestionResponseDTO>> updateSubjective(
		@PathVariable Long questionId,
		@Valid @ModelAttribute LeveltestSubjectiveRequestDTO request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) {
		LeveltestQuestionResponseDTO dto = commandService.updateTextAnswerQuestion(questionId, request, image);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(summary = "문제 삭제")
	@DeleteMapping("/{questionId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<Long>> deleteQuestion(@PathVariable Long questionId) {
		commandService.deleteQuestion(questionId);
		return ResponseEntity.ok(BaseResponse.onSuccess(questionId));
	}

	@Operation(summary = "문제 상세 조회", description = "problemType에 따라 필요한 필드만 포함하여 반환합니다.")
	@GetMapping("/{questionId}")
	public ResponseEntity<BaseResponse<LevelTestQuestionDetailResponseDTO>> getQuestionDetail(
		@PathVariable Long questionId
	) {
		LevelTestQuestionDetailResponseDTO dto = queryService.getQuestion(questionId);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(
		summary = "문제 전체 조회(무한스크롤)",
		description = "DB에 있는 모든 문제를 무한 스크롤로 반환합니다. 예) ?page=0&size=10&sort=createdAt,desc"
	)
	@GetMapping
	public ResponseEntity<BaseResponse<Slice<LevelTestQuestionDetailResponseDTO>>> getQuestions(
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Slice<LevelTestQuestionDetailResponseDTO> slice = queryService.getQuestions(pageable);
		return ResponseEntity.ok(BaseResponse.onSuccess(slice));
	}

	@Operation(summary = "레벨테스트 시도 목록 조회 (상태별)")
	@GetMapping("attempts")
	public ResponseEntity<BaseResponse<Page<AdminLeveltestAttemptListResponseDTO>>> getAttempts(
		@RequestParam(defaultValue = "GRADING") LevelTestStaus status,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<AdminLeveltestAttemptListResponseDTO> page = queryService.getAttemptsByStatus(status, pageable);
		return ResponseEntity.ok(BaseResponse.onSuccess(page));
	}

	@Operation(summary = "레벨테스트 시도 상세 조회")
	@GetMapping("/attempts/{attemptId}")
	public ResponseEntity<BaseResponse<AdminLeveltestAttemptDetailResponseDTO>> getAttemptDetail(
		@PathVariable Long attemptId) {
		AdminLeveltestAttemptDetailResponseDTO dto = queryService.getAttemptDetail(attemptId);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(summary = "수동 채점 반영")
	@PostMapping("/attempts/{attemptId}/grade")
	public ResponseEntity<BaseResponse<Long>> gradeManually(
		@PathVariable Long attemptId,
		@RequestBody @Valid LeveltestGradeRequestDTO request
	) {
		commandService.applyManualGrading(attemptId, request);
		return ResponseEntity.ok(BaseResponse.onSuccess(attemptId));
	}
}
