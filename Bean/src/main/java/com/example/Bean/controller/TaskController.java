package com.example.Bean.controller;

import com.example.Bean.dto.TaskDto;
import com.example.Bean.model.Task;
import com.example.Bean.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    // For testing, use the existing user ID
    private final Long userId = 1L;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Display all tasks for the user
    @GetMapping
    public String viewTasks(Model model) {
        List<Task> tasks = taskService.getTasksByUser(userId);
        model.addAttribute("tasks", tasks);
        return "task";
    }

    // Show form to add a new task
    @GetMapping("/add")
    public String addTaskForm(Model model) {
        model.addAttribute("task", new TaskDto());
        return "addtask";
    }

    // Save a new task
    @PostMapping("/save")
    public String saveTask(@ModelAttribute TaskDto taskDto) {
        // Use hardcoded userId for now
        taskService.createTaskFromDto(taskDto, userId);
        return "redirect:/task"; // corrected redirect path
    }

    // Search tasks
    @GetMapping("/search")
    public String searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            Model model) {

        List<Task> results = taskService.searchTasks(userId, keyword, priority, status);
        model.addAttribute("tasks", results);
        return "task";
    }

    @GetMapping("/{id}/toggle")
    public String toggleTaskComplete(@PathVariable Long id) {
        taskService.toggleComplete(id);
        return "redirect:/task";
    }



}
