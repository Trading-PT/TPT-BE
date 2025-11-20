package com.tradingpt.tpt_api.domain.lecture.service.command;

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
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.ast.tree.update.Assignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LectureCommandServiceImpl implements LectureCommandService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final S3FileService s3FileService;

    @Override
    @Transactional
    public Long purchaseLecture(Long lectureId, Long userId) {

        // 1) 유저 조회
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

        // 2) 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 3) 유료 강의는 구매 불가
        if (lecture.getRequiredTokens() == 0) {
            throw new LectureException(LectureErrorStatus.ALREADY_FREE_LECTURE);
        }

        // 4) 이미 수강한 경우 구매 불가
        boolean alreadyPurchased =
                lectureProgressRepository.existsByLectureIdAndCustomerId(lectureId, userId);

        if (alreadyPurchased) {
            throw new LectureException(LectureErrorStatus.ALREADY_PURCHASED);
        }

        // 5) 유저 토큰 보유량 체크
        if (customer.getToken() < lecture.getRequiredTokens()) {
            throw new LectureException(LectureErrorStatus.NOT_ENOUGH_TOKENS);
        }

        // 6) 토큰 차감
        customer.useTokens(lecture.getRequiredTokens());

        // 7) LectureProgress 생성 (구매 완료)
        LectureProgress progress = LectureProgress.builder()
                .lecture(lecture)
                .customer(customer)
                .watchedSeconds(0)
                .lastPositionSeconds(0)
                .isCompleted(false)
                .build();

        lectureProgressRepository.save(progress);

        return customer.getId();
    }

    @Override
    @Transactional
    public void updateLectureProgress(Long userId, Long lectureId, int currentSeconds) {

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        int duration = lecture.getDurationSeconds();

        LectureProgress progress = lectureProgressRepository
                .findByLectureIdAndCustomerId(lectureId, customer.getId())
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 1) 개별 강의 진도 업데이트
        progress.updateProgress(currentSeconds, duration);


        //  2) PRO 전체 강의 중 몇 개가 completed인지 체크
        int completedCount = lectureProgressRepository.countCompletedProLectures(customer.getId());
        int totalProLectures = lectureRepository.countProLectures();


        //  3) 전체 완강했으면 Customer 상태를 BEFORE → PENDING 으로 변경
        if (completedCount == totalProLectures &&
                customer.getCourseStatus() == CourseStatus.BEFORE_COMPLETION) {

            customer.updateCourseStatus(CourseStatus.PENDING_COMPLETION);
        }
    }


    /**
     * 과제 제출
     */
    @Override
    public Long submitAssignment(Long userId, Long lectureId, MultipartFile file) {

        // 1. 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));

        // 2. 고객 조회
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        // 3. 기존 제출 내역 있는지 확인
        CustomerAssignment assignment = customerAssignmentRepository
                .findByLectureIdAndCustomerId(lectureId, userId)
                .orElseGet(() -> CustomerAssignment.builder()
                        .lecture(lecture)
                        .customer(customer)
                        .submitted(false)
                        .build()
                );

        // 4. S3 업로드 (과제 PDF)
        String directory = "assignments/" + lectureId;
        var uploadResult = s3FileService.upload(file, directory);

        // 5. 기존 파일 있으면 삭제 + 재저장
        assignment.markSubmitted();
        CustomerAssignment saved = customerAssignmentRepository.save(assignment);

        // 첨부파일은 항상 1개만 관리한다고 가정
        assignmentAttachmentRepository.findByCustomerAssignmentId(saved.getId())
                .ifPresent(old -> {
                    s3FileService.delete(old.getFileKey());
                    assignmentAttachmentRepository.delete(old);
                });

        AssignmentAttachment attachment = AssignmentAttachment.builder()
                .customerAssignment(saved)
                .fileUrl(uploadResult.url())
                .fileKey(uploadResult.key())
                .build();

        assignmentAttachmentRepository.save(attachment);

        return saved.getId();
    }
}

