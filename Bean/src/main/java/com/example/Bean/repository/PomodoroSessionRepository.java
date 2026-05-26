package com.example.Bean.repository;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    List<PomodoroSession> findByUserId(Long userId);

    List<PomodoroSession> findByUserIdAndStartTimeBetween(
            Long userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    int countByUserIdAndStartTimeBetween(
            Long userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    // sum of focus seconds (completed sessions only) in a time range
    @Query("""
           SELECT COALESCE(SUM(p.durationSeconds), 0)
           FROM PomodoroSession p
           WHERE p.user.id = :userId
             AND p.isCompleted = true
             AND p.startTime >= :start
             AND p.startTime < :end
           """)
    Long sumDurationBetween(@Param("userId") Long userId,
                            @Param("start") OffsetDateTime start,
                            @Param("end") OffsetDateTime end);

    @Query("""
           SELECT COALESCE(SUM(p.durationSeconds), 0)
           FROM PomodoroSession p
           WHERE p.user.id = :userId
             AND p.type = :type
             AND p.isCompleted = true
             AND p.startTime >= :start
             AND p.startTime < :end
           """)
    Long sumDurationByTypeBetween(@Param("userId") Long userId,
                                  @Param("type") PomodoroType type,
                                  @Param("start") OffsetDateTime start,
                                  @Param("end") OffsetDateTime end);
}
