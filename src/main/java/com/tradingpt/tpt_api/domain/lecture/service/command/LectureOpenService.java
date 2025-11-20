package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureOpenService {

    private final SubscriptionRepository subscriptionRepository;
    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;

    /**
     * 스케줄러(예: 0시)에 돌면서 ACTIVE 구독자들의 주차를 채워주는 메서드
     */
    @Transactional
    public void openWeeklyForActiveSubscriptions() {
        List<Subscription> actives = subscriptionRepository
                .findAllByStatus(Status.ACTIVE);

        for (Subscription sub : actives) {
            openWeeklyForSubscription(sub);
        }
    }

    private void openWeeklyForSubscription(Subscription sub) {

        Customer customer = sub.getCustomer();
        Long customerId = customer.getId();

        // 0) 구독 시작 전이면 열지 않음 (방어)
        LocalDate start = sub.getCurrentPeriodStart();
        if (LocalDate.now().isBefore(start)) {
            return;
        }

        // 1) PRO 챕터에 속한 강의들만, 순서대로 가져오기
        List<Lecture> allLectures = lectureRepository.findAllOrderByChapterAndLectureOrder();

        List<Lecture> proLectures = new ArrayList<>();
        for (Lecture l : allLectures) {
            if (l.getChapter().getChapterType() == ChapterType.PRO) {
                proLectures.add(l);
            }
        }

        if (proLectures.isEmpty()) {
            return; // 열 PRO 강의가 없으면 끝
        }

        // 2) 지금까지 열린 PRO 강의 개수 (index 개념)
        int openedCount = (customer.getOpenChapterNumber() == null)
                ? 0
                : customer.getOpenChapterNumber();

        // 이미 모든 PRO 강의가 열려 있으면 종료 (완강)
        if (openedCount >= proLectures.size()) {
            return;
        }

        // 3) 이번 주에 열어줄 "다음 강의" = proLectures[openedCount]
        Lecture nextLecture = proLectures.get(openedCount);

        // 혹시 이미 열려 있다면(중복 방어) 그냥 skip
        boolean exists = lectureProgressRepository
                .existsByLectureIdAndCustomerId(nextLecture.getId(), customerId);
        if (!exists) {
            lectureProgressRepository.save(
                    LectureProgress.builder()
                            .lecture(nextLecture)
                            .customer(customer)
                            .watchedSeconds(0)
                            .isCompleted(false)
                            .build()
            );
        }

        // 4) 열린 강의 개수 +1
        customer.updateOpenChapterNumber(openedCount + 1);
    }
}

