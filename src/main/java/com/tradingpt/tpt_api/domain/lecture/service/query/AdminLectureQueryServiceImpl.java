package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.LectureListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.CustomerHomeworkSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.AssignmentAttachment;
import com.tradingpt.tpt_api.domain.lecture.entity.CustomerAssignment;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.AssignmentAttachmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.ChapterRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.CustomerAssignmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLectureQueryServiceImpl implements AdminLectureQueryService {

    private final LectureRepository lectureRepository;
    private final ChapterRepository chapterRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final S3FileService s3FileService;

    @Override
    @Transactional(readOnly = true)
    public Page<LectureListResponseDTO> getLectureList(Pageable pageable, String category) {
        LectureExposure exposure = parseCategory(category);

        Page<Lecture> page;
        if (exposure == null) {
            page = lectureRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            page = lectureRepository.findByLectureExposureOrderByCreatedAtDesc(exposure, pageable);
        }

        return page.map(LectureListResponseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterListResponseDTO> getAllChapters() {
        return chapterRepository.findAllSimple();
    }

    @Override
    @Transactional(readOnly = true)
    public LectureDetailResponseDTO getLectureDetail(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));
        return LectureDetailResponseDTO.from(lecture);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerHomeworkSummaryResponseDTO getCustomerHomeworkSummary(Long customerId) {

        // 1. 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        // 2. PRO 강의 정렬하여 가져오기
        List<Lecture> proLectures = lectureRepository.findAllOrderByChapterAndLectureOrder()
                .stream()
                .filter(l -> l.getChapter().getChapterType() == ChapterType.PRO)
                .toList();

        // 3. 열린 최대 lectureOrder (= openChapterNumber)
        int openedMaxOrder = (customer.getOpenChapterNumber() == null)
                ? 0
                : customer.getOpenChapterNumber();

        int notSubmittedCount = 0;
        List<CustomerHomeworkSummaryResponseDTO.CustomerHomeworkItemDTO> items = new ArrayList<>();

        // 4. 각 PRO 강의에 대한 제출 정보 구성
        for (int i = 0; i < proLectures.size(); i++) {

            Lecture lecture = proLectures.get(i);
            int order = i + 1;
            Integer lectureOrder = lecture.getLectureOrder();
            int lo = (lectureOrder == null ? 0 : lectureOrder);

            String status;
            List<CustomerHomeworkSummaryResponseDTO.SubmissionDTO> submissions = new ArrayList<>();

            // 🔥 lectureOrder 기준으로 열림 여부 판단
            if (lo == 0 || lo > openedMaxOrder) {
                status = "수강 전"; // 아직 열리지 않은 강의
            } else {
                // 🔥 이미 열린 강의 → 과제 제출 조회
                CustomerAssignment assignment = customerAssignmentRepository
                        .findByLectureIdAndCustomerId(lecture.getId(), customerId)
                        .orElse(null);

                if (assignment == null) {
                    status = "미제출";
                    notSubmittedCount++;

                } else {
                    // 제출한 첨부파일(여러 attempt) 조회
                    List<AssignmentAttachment> attachments =
                            assignmentAttachmentRepository
                                    .findAllByCustomerAssignmentIdOrderByAttemptNoAsc(assignment.getId());

                    if (attachments.isEmpty()) {
                        status = "미제출";
                        notSubmittedCount++;

                    } else {
                        status = "제출";

                        for (AssignmentAttachment att : attachments) {

                            String downloadUrl = s3FileService.createPresignedGetUrl(
                                    att.getFileKey(),
                                    Duration.ofMinutes(60) // 60분짜리 URL
                            );
                            submissions.add(
                                    CustomerHomeworkSummaryResponseDTO.SubmissionDTO.builder()
                                            .attemptNo(att.getAttemptNo())
                                            .fileName(extractFileNameFromKey(att.getFileKey()))
                                            .downloadUrl(downloadUrl)
                                            .submittedAt(att.getCreatedAt())
                                            .build()
                            );
                        }
                    }
                }
            }

            // 5. DTO 구성
            items.add(
                    CustomerHomeworkSummaryResponseDTO.CustomerHomeworkItemDTO.builder()
                            .lectureId(lecture.getId())
                            .order(order)
                            .lectureTitle(lecture.getTitle())
                            .status(status)
                            .submissions(submissions)
                            .build()
            );
        }

        // 6. 실제 열린 강의 수 계산 (lectureOrder 기준)
        int totalOpenedCount = (int) proLectures.stream()
                .filter(l -> {
                    Integer lo = l.getLectureOrder();
                    return lo != null && lo > 0 && lo <= openedMaxOrder;
                })
                .count();

        // 7. 최종 반환
        return CustomerHomeworkSummaryResponseDTO.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .totalOpenedCount(totalOpenedCount)
                .notSubmittedCount(notSubmittedCount)
                .items(items)
                .build();
    }



    /**
     * "All" 이면 null 리턴해서 필터 안 걸고,
     * 그 외인데 enum에 없으면 예외 던진다.
     */
    private LectureExposure parseCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return null;
        }
        try {
            return LectureExposure.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new LectureException(LectureErrorStatus.INVALID_CATEGORY);
        }
    }

    /**
     * S3 object key 기준으로 파일명만 추출
     * 예: "assignments/2025-11-27/uuid1234.pdf" -> "uuid1234.pdf"
     */
    private String extractFileNameFromKey(String key) {
        if (key == null) return null;
        int idx = key.lastIndexOf('/');
        return (idx == -1) ? key : key.substring(idx + 1);
    }
}
