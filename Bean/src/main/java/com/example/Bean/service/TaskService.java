package com.example.Bean.service;

import com.example.Bean.dto.TaskDto;
import com.example.Bean.enums.Priority;
import com.example.Bean.enums.TaskStatus;
import com.example.Bean.model.Task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
public interface TaskService {

    Task createTaskFromDto(TaskDto dto, Long userId);

    List<Task> getTasksByUser(Long userId);

    List<Task> searchTasks(Long userId, String keyword, String priority, String status);

    Map<String, Integer> getSummary(Long userId);

    List<Task> getTodayTasks(Long userId);

    void toggleComplete(Long taskId);
}
