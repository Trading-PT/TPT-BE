package com.tradingpt.tpt_api.domain.event.controller;

import com.tradingpt.tpt_api.domain.event.dto.request.EventCreateRequestDTO;
import com.tradingpt.tpt_api.domain.event.dto.request.EventUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.event.dto.response.EventResponseDTO;
import com.tradingpt.tpt_api.domain.event.service.command.AdminEventCommandService;
import com.tradingpt.tpt_api.domain.event.service.query.AdminEventQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Tag(name = "관리자 이벤트", description = "이벤트 설정 관련 API (관리자용)")
public class AdminEventV1Controller {

    private final AdminEventCommandService adminEventCommandService;
    private final AdminEventQueryService adminEventQueryService;

    @Operation(summary = "이벤트 생성(관리자)", description = "어드민이 이벤트를 생성합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createEvent(
            @RequestBody @Valid EventCreateRequestDTO request
    ) {
        Long id = adminEventCommandService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.onSuccessCreate(id));
    }

    @Operation(summary = "이벤트 목록 조회(관리자)",
            description = "이벤트 목록을 조회합니다. onlyActive=true면 활성 이벤트만 조회합니다. " +
                    "예) ?onlyActive=true&page=0&size=20&sort=startAt,asc")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<EventResponseDTO>>> getEvents(
            @RequestParam(required = false, defaultValue = "false") boolean onlyActive,
            @PageableDefault(size = 10, sort = "startAt", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<EventResponseDTO> page = adminEventQueryService.getEvents(pageable, onlyActive);
        return ResponseEntity.ok(BaseResponse.onSuccess(page));
    }

    @Operation(summary = "이벤트 단건 조회(관리자)")
    @GetMapping("/{eventId}")
    public ResponseEntity<BaseResponse<EventResponseDTO>> getEvent(
            @PathVariable Long eventId
    ) {
        EventResponseDTO dto = adminEventQueryService.getEvent(eventId);
        return ResponseEntity.ok(BaseResponse.onSuccess(dto));
    }

    @Operation(summary = "이벤트 수정(관리자)", description = "이벤트 정보를 수정합니다.")
    @PutMapping("/{eventId}")
    public ResponseEntity<BaseResponse<Long>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid EventUpdateRequestDTO request
    ) {
        Long updatedId = adminEventCommandService.updateEvent(eventId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(updatedId));
    }

    @Operation(summary = "이벤트 삭제(관리자)", description = "이벤트를 삭제합니다. (하드 삭제)")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<BaseResponse<Long>> deleteEvent(
            @PathVariable Long eventId
    ) {
        Long deletedId = adminEventCommandService.deleteEvent(eventId);
        return ResponseEntity.ok(BaseResponse.onSuccess(deletedId));
    }
}
