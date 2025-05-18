package org.example.repository;

import org.example.entity.Booking;
import org.example.entity.User;
import org.example.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserAndCancelledFalse(User user);

    List<Booking> findByWorkspaceAndCancelledFalseAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        Workspace workspace, LocalDateTime end, LocalDateTime start
    );

    boolean existsByWorkspaceAndCancelledFalseAndStartTimeLessThanAndEndTimeGreaterThan(
        Workspace workspace, LocalDateTime end, LocalDateTime start
    );

    List<Booking> findAllByEndTimeBeforeAndCancelledFalse(LocalDateTime time);

    @Query("""
    select b.workspace.id from Booking b
    where b.cancelled = false
      and b.workspace.active = true
      and (:end > b.startTime and :start < b.endTime)
    """)
    List<Long> findBusyWorkspaceIdsBetween(LocalDateTime start, LocalDateTime end);

}
