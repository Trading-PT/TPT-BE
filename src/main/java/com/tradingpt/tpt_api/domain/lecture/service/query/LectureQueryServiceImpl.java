package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureQueryServiceImpl implements LectureQueryService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;

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
}
