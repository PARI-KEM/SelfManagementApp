package com.example.Bean.service.impl;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;
import com.example.Bean.model.Task;
import com.example.Bean.model.User;
import com.example.Bean.repository.PomodoroSessionRepository;
import com.example.Bean.service.PomodoroService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class PomodoroServiceImpl implements PomodoroService {

    private final PomodoroSessionRepository pomodoroRepository;

    public PomodoroServiceImpl(PomodoroSessionRepository pomodoroRepository) {
        this.pomodoroRepository = pomodoroRepository;
    }

    // -------------------- START SESSION --------------------
    @Override
    public PomodoroSession startSession(Long userId, PomodoroType type, Long taskId) {
        PomodoroSession session = new PomodoroSession();

        // Link user by ID without loading full entity
        User user = new User();
        user.setId(userId);
        session.setUser(user);

        // Optional task
        if (taskId != null) {
            Task task = new Task();
            task.setId(taskId);
            session.setTask(task);
        }

        session.setType(type);
        session.setStartTime(OffsetDateTime.now());
        session.setIsCompleted(false);

        return pomodoroRepository.save(session);
    }

    // -------------------- END SESSION --------------------
    @Override
    public PomodoroSession endSession(Long sessionId) {
        PomodoroSession session = pomodoroRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setEndTime(OffsetDateTime.now());
        session.setIsCompleted(true);

        long duration = session.getEndTime().toEpochSecond() - session.getStartTime().toEpochSecond();
        session.setDurationSeconds((int) duration);

        return pomodoroRepository.save(session);
    }

    // -------------------- COUNT TODAY --------------------
    @Override
    public int countTodayPomodoros(Long userId) {
        LocalDate today = LocalDate.now();

        OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return pomodoroRepository.countByUserIdAndStartTimeBetween(
                userId,
                startOfDay,
                endOfDay
        );
    }
}
