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

    /**
     * 특정 회원 PRO 강의 과제 현황 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerHomeworkSummaryResponseDTO getCustomerHomeworkSummary(Long customerId) {

        // 1. 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        // 2. PRO 챕터 강의들을 정렬된 리스트로 가져오기
        List<Lecture> allLectures = lectureRepository.findAllOrderByChapterAndLectureOrder();

        List<Lecture> proLectures = new ArrayList<>();
        for (Lecture lecture : allLectures) {
            if (lecture.getChapter().getChapterType() == ChapterType.PRO) {
                proLectures.add(lecture);
            }
        }

        // 3. 지금까지 열린 PRO 강의 개수 (null 이면 0)
        int openedCount = (customer.getOpenChapterNumber() == null)
                ? 0
                : customer.getOpenChapterNumber();

        // 열린 개수는 PRO 강의 총 개수를 넘지 않도록 조정
        int totalOpenedCount = Math.min(openedCount, proLectures.size());

        int notSubmittedCount = 0;
        List<CustomerHomeworkSummaryResponseDTO.CustomerHomeworkItemDTO> items = new ArrayList<>();

        // 4. PRO 강의를 순회하면서 상태/제출 파일 정보 계산
        for (int i = 0; i < proLectures.size(); i++) {

            Lecture lecture = proLectures.get(i);
            int order = i + 1; // 1-based 표시

            String status;
            String submittedFileUrl = null;
            String submittedFileName = null;

            if (i >= totalOpenedCount) {
                // 아직 열리지 않은 강의
                status = "수강 전";
            } else {
                // 이미 열린 강의 → 제출 여부 확인
                CustomerAssignment assignment = customerAssignmentRepository
                        .findByLectureIdAndCustomerId(lecture.getId(), customerId)
                        .orElse(null);

                boolean submitted = (assignment != null && assignment.isSubmitted());

                if (submitted) {
                    status = "제출";

                    // 제출된 과제 첨부파일 조회 (1개만 관리)
                    AssignmentAttachment attachment = assignmentAttachmentRepository
                            .findByCustomerAssignmentId(assignment.getId())
                            .orElse(null);

                    if (attachment != null) {
                        submittedFileUrl = attachment.getFileUrl();
                        submittedFileName = extractFileName(submittedFileUrl);
                    }
                } else {
                    status = "미제출";
                    notSubmittedCount++;
                }
            }

            items.add(CustomerHomeworkSummaryResponseDTO.CustomerHomeworkItemDTO.builder()
                    .lectureId(lecture.getId())
                    .order(order)
                    .lectureTitle(lecture.getTitle())
                    .status(status)
                    .submittedFileName(submittedFileName)
                    .submittedFileUrl(submittedFileUrl)
                    .build());
        }

        // 5. 요약 정보와 함께 반환
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

    private String extractFileName(String url) {
        if (url == null) return null;
        int idx = url.lastIndexOf('/');
        return (idx == -1) ? url : url.substring(idx + 1);
    }
}
