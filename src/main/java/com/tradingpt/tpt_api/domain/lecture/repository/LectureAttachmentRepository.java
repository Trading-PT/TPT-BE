package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.entity.LectureAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureAttachmentRepository extends JpaRepository<LectureAttachment, Long> {
}
