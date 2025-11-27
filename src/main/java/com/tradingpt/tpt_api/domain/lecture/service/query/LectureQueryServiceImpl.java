package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.AssignmentAttachmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.CustomerAssignmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureQueryServiceImpl implements LectureQueryService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final CustomerRepository customerRepository;
    private final S3FileService s3FileService;

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

        return LectureDetailDTO.from(lecture, progress);
    }

    @Override
    public AssignmentSubmissionDetailDTO getMyAssignmentDetail(Long userId, Long lectureId) {

        // 1. 제출 엔티티 찾기
        CustomerAssignment assignment = customerAssignmentRepository
                .findByLectureIdAndCustomerId(lectureId, userId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.ASSIGNMENT_NOT_SUBMITTED));

        // 2. 제출 첨부파일 찾기
        AssignmentAttachment attachment = assignmentAttachmentRepository
                .findByCustomerAssignmentId(assignment.getId())
                .orElse(null);

        // 3. DTO 변환 (from 메서드)
        return AssignmentSubmissionDetailDTO.from(assignment, attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public LecturePlayResponseDTO getLecturePlayUrl(Long userId, Long lectureId) {
        // 1) 유저/강의 존재 체크
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 2) videoKey로 presigned GET URL 생성
        String videoKey = lecture.getVideoKey();
        if (videoKey == null || videoKey.isBlank()) {
            throw new LectureException(LectureErrorStatus.VIDEO_NOT_FOUND);
        }

        var duration = java.time.Duration.ofHours(3); // 3시간짜리 URL
        String playUrl = s3FileService.createPresignedGetUrl(videoKey, duration);

        return LecturePlayResponseDTO.builder()
                .playUrl(playUrl)
                .expiresInSeconds(duration.toSeconds())
                .build();
    }
}
