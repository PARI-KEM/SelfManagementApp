package com.example.Bean.service.impl;

import com.example.Bean.dto.TaskDto;
import com.example.Bean.enums.Priority;
import com.example.Bean.enums.TaskStatus;
import com.example.Bean.model.Task;
import com.example.Bean.model.User;
import com.example.Bean.repository.TaskRepository;
import com.example.Bean.repository.UserRepository;
import com.example.Bean.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskServiceImpl(TaskRepository taskRepo,
                           UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public Task createTaskFromDto(TaskDto dto, Long userId) {
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Task task = new Task();
            task.setUser(user);
            task.setTitle(dto.getTitle());
            task.setDescription(dto.getDescription());
            task.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);

            // Safe conversion from HTML datetime-local (yyyy-MM-dd'T'HH:mm)
            if (dto.getDueDate() != null && !dto.getDueDate().isEmpty()) {
                LocalDateTime ldt = LocalDateTime.parse(dto.getDueDate());
                task.setDueDate(ldt.atOffset(ZoneOffset.ofHoursMinutes(5, 30)));
            }

            task.setStatus(TaskStatus.TODO);
            task.setStartTime(OffsetDateTime.now());
            task.setReminderAt(null);
            task.setCategory(null);

            return taskRepo.save(task);
        } catch (Exception e) {
            e.printStackTrace(); // logs full exception for debugging
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    @Override
    public List<Task> getTasksByUser(Long userId) {
        try {
            return taskRepo.findByUser_Id(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch tasks for user: " + e.getMessage());
        }
    }

    @Override
    public List<Task> searchTasks(Long userId, String keyword, String priority, String status) {
        try {
            TaskStatus st = (status != null && !status.isBlank()) ? TaskStatus.valueOf(status) : null;
            Priority pr = (priority != null && !priority.isBlank()) ? Priority.valueOf(priority) : null;

            return taskRepo.filterTasks(userId, st, pr, keyword);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid status or priority value: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to search tasks: " + e.getMessage());
        }
    }
    @Override
    public List<Task> getTodayTasks(Long userId) {
        // Get today's start and end in UTC (or your timezone)
        LocalDate today = LocalDate.now();
        OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return taskRepo.findTodayTasks(userId, startOfDay, endOfDay);
    }
    @Override
    public Map<String, Integer> getSummary(Long userId) {
        try {
            int today = taskRepo.countByUserIdAndDueDate(userId, LocalDate.now());
            int pending = taskRepo.countByUser_IdAndStatus(userId, TaskStatus.TODO);
            int completed = taskRepo.countByUser_IdAndStatus(userId, TaskStatus.COMPLETED);

            return Map.of(
                    "tasksToday", today,
                    "pending", pending,
                    "completed", completed
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch summary: " + e.getMessage());
        }
    }
    @Override
    public void toggleComplete(Long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepo.deleteById(taskId);

    }

}
