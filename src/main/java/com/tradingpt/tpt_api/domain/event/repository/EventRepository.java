package com.tradingpt.tpt_api.domain.event.repository;

import com.tradingpt.tpt_api.domain.event.entity.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByActiveTrue(Pageable pageable);

    List<Event> findAllByActiveTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqual(LocalDateTime start, LocalDateTime end);

}
