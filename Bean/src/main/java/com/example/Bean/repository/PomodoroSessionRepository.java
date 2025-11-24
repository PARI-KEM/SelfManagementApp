package com.example.Bean.repository;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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
}

