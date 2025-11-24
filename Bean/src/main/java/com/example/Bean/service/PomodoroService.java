package com.example.Bean.service;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;

public interface PomodoroService {
    int countTodayPomodoros(Long userId);
    PomodoroSession startSession(Long userId, PomodoroType type,Long taskId);

    PomodoroSession endSession(Long sessionId);
}
