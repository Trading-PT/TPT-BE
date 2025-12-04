package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionHistoryItemDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureAttachmentDownloadResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachmentDownloadHistory;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.AssignmentAttachmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.CustomerAssignmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureAttachmentDownloadHistoryRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureAttachmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.CloudFrontService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureQueryServiceImpl implements LectureQueryService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureAttachmentRepository lectureAttachmentRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final LectureAttachmentDownloadHistoryRepository downloadHistoryRepository;
    private final CustomerRepository customerRepository;
    private final CloudFrontService cloudFrontService;

    @Override
    public List<ChapterBlockDTO> getCurriculum(Long userId, int page, int size) {
        return lectureRepository.findCurriculum(userId, page, size);
    }

    @Override
    public LectureDetailDTO getLectureDetail(Long userId, Long lectureId) {

        Lecture lecture = lectureRepository.findByIdWithAttachments(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        LectureProgress progress = lectureProgressRepository
                .findByLectureIdAndCustomerId(lectureId, userId)
                .orElse(null);   //소유한 강의가 아닌경우에 progress가 없으므로 null반환(예외 터뜨리면 안됌)

        if (progress != null && progress.getDueDate() != null &&
                progress.getDueDate().isBefore(LocalDateTime.now())) {
            throw new LectureException(LectureErrorStatus.LECTURE_EXPIRED);
        }

        return LectureDetailDTO.from(lecture, progress);
    }

    @Override
    @Transactional(readOnly = true)
    public LecturePlayResponseDTO getLecturePlayUrl(Long userId, Long lectureId, String clientIp) {

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        LectureProgress lectureProgress = lectureProgressRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.PROGRESS_NOT_FOUND));

        Duration duration = Duration.ofHours(3);

        String signedUrl = cloudFrontService.createSignedUrl(
                lecture.getVideoKey(),
                duration,
                clientIp
        );

        return LecturePlayResponseDTO.builder()
                .playUrl(signedUrl)
                .expiresInSeconds(duration.toSeconds())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionHistoryItemDTO> getMyAssignmentSubmissionHistory(
            Long userId,
            Long lectureId,
            HttpServletRequest request
    ) {

        // 1) 해당 강의 + 유저의 과제 엔티티 찾기
        CustomerAssignment assignment = customerAssignmentRepository
                .findByLectureIdAndCustomerId(lectureId, userId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.ASSIGNMENT_NOT_SUBMITTED));

        // 2) 이 과제에 대한 모든 제출(첨부) 이력 조회 (최신부터)
        List<AssignmentAttachment> attachments =
                assignmentAttachmentRepository.findAllByCustomerAssignmentIdOrderByAttemptNoDesc(assignment.getId());

        String clientIp = extractClientIp(request); // 이미 있던 util 메서드 사용

        // 3) 각 제출마다 서명 URL 만들어서 DTO 로 변환
        return attachments.stream()
                .map(att -> {
                    String signedUrl = cloudFrontService.createSignedUrl(
                            att.getFileKey(),
                            Duration.ofMinutes(10),
                            clientIp
                    );
                    return AssignmentSubmissionHistoryItemDTO.of(assignment, att, signedUrl);
                })
                .toList();
    }

    public LectureAttachmentDownloadResponseDTO getLectureAttachmentDownloadUrl(
            Long userId,
            Long lectureId,
            Long attachmentId,
            String clientIp
    ) {

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 첨부파일 조회 + 해당 강의 소속인지 검증
        LectureAttachment attachment = lectureAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.ATTACHMENT_NOT_FOUND));

        if (!attachment.getLecture().getId().equals(lecture.getId())) {
            throw new LectureException(LectureErrorStatus.INVALID_ATTACHMENT_FOR_LECTURE);
        }

        Duration duration = Duration.ofMinutes(10);

        String signedUrl = cloudFrontService.createSignedUrl(
                attachment.getFileKey(),
                duration,
                clientIp
        );

        // 다운로드 이력 저장
        LectureAttachmentDownloadHistory history = LectureAttachmentDownloadHistory.builder()
                .customer(customer)
                .lectureAttachment(attachment)
                .build();

        downloadHistoryRepository.save(history);

        return LectureAttachmentDownloadResponseDTO.builder()
                .attachmentId(attachment.getId())
                .downloadUrl(signedUrl)
                .expiresInSeconds(duration.toSeconds())
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // "ip1, ip2, ..." 형태 → 첫 번째가 클라이언트 IP
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
