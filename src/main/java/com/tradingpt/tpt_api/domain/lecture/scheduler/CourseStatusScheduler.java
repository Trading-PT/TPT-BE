package com.tradingpt.tpt_api.domain.lecture.scheduler;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CourseStatusScheduler {

    private final CustomerRepository customerRepository;

    // 매달 1일 새벽 1시
    @Scheduled(cron = "0 0 1 1 * *")
    @SchedulerLock(
            name = "courseStatusMonthlyJob",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT10M"
    )
    @Transactional
    public void updateCompletedUsers() {

        List<Customer> pendingUsers =
                customerRepository.findAllByCourseStatus(CourseStatus.PENDING_COMPLETION);

        for (Customer user : pendingUsers) {
            user.updateCourseStatus(CourseStatus.AFTER_COMPLETION);
        }
    }
}

