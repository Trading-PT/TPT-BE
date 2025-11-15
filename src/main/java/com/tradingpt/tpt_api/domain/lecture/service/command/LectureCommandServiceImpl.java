package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureCommandServiceImpl implements LectureCommandService {

    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CustomerRepository customerRepository;

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

        progress.updateProgress(currentSeconds, duration);
    }
}

