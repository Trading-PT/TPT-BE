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
import java.time.temporal.ChronoUnit;
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

    @Transactional
    public void openWeeklyForSubscription(Subscription sub) {

        Customer customer = sub.getCustomer();
        Long customerId = customer.getId();

        // (0) 구독 시작 날짜
        LocalDate start = sub.getCurrentPeriodStart();
        LocalDate today = LocalDate.now();
        if (today.isBefore(start)) {
            return;
        }

        // (1) 경과 일수
        long days = ChronoUnit.DAYS.between(start, today);
//
//        // (2) 경과 주차
//        int weeksPassed = (int) (days / 7);
//
//        // (3) 열려 있어야 하는 총 강의 개수 = 주차 + 1
//        int shouldOpenCount = weeksPassed + 1;
//        int shouldOpenCount = (int) days + 1;

        // 👉 스케줄러 실행 시마다 딱 1개씩만 열리게 강제
        int openedCount = (customer.getOpenChapterNumber() == null)
                ? 0
                : customer.getOpenChapterNumber();

        int shouldOpenCount = openedCount + 1;  // 딱 1개만 증가

        // (4) PRO 강의 목록
        List<Lecture> allLectures = lectureRepository.findAllOrderByChapterAndLectureOrder();
        List<Lecture> proLectures = new ArrayList<>();

        for (Lecture l : allLectures) {
            if (l.getChapter().getChapterType() == ChapterType.PRO) {
                proLectures.add(l);
            }
        }

        if (proLectures.isEmpty()) {
            return;
        }

        // (5) 현재 열린 개수
        openedCount = (customer.getOpenChapterNumber() == null)
                ? 0
                : customer.getOpenChapterNumber();

        // (6) 열려야 하는 것이 더 많을 때만 오픈
        while (openedCount < shouldOpenCount && openedCount < proLectures.size()) {

            Lecture next = proLectures.get(openedCount);

            boolean exists = lectureProgressRepository.existsByLectureIdAndCustomerId(next.getId(), customerId);
            if (!exists) {
                lectureProgressRepository.save(
                        LectureProgress.builder()
                                .lecture(next)
                                .customer(customer)
                                .watchedSeconds(0)
                                .isCompleted(false)
                                .build()
                );
            }

            openedCount++;
            customer.updateOpenChapterNumber(openedCount);
        }
    }
}

