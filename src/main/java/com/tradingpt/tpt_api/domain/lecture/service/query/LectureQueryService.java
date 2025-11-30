package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.response.AssignmentSubmissionHistoryItemDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureAttachmentDownloadResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LecturePlayResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface LectureQueryService {
    List<ChapterBlockDTO> getCurriculum(Long userId, int page, int size);
    LectureDetailDTO getLectureDetail(Long userId, Long lectureId);
    LecturePlayResponseDTO getLecturePlayUrl(Long userId, Long lectureId, String clientIp);
    List<AssignmentSubmissionHistoryItemDTO> getMyAssignmentSubmissionHistory( Long userId, Long lectureId, HttpServletRequest request);
    LectureAttachmentDownloadResponseDTO getLectureAttachmentDownloadUrl(Long userId, Long lectureId, Long attachmentId, String clientIp);
}

