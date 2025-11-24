package com.example.Bean.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.Bean.enums.PomodoroType;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pomodoro_sessions")
@Getter
@Setter
public class PomodoroSession extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task; // optional

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private PomodoroType type = PomodoroType.FOCUS;
}
