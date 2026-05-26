package com.example.Bean.controller;

import com.example.Bean.dto.TaskDto;
import com.example.Bean.model.Task;
import com.example.Bean.security.CurrentUser;
import com.example.Bean.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUser currentUser;

    public TaskController(TaskService taskService, CurrentUser currentUser) {
        this.taskService = taskService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String viewTasks(Model model) {
        List<Task> tasks = taskService.getTasksByUser(currentUser.requireId());
        model.addAttribute("tasks", tasks);
        return "task";
    }

    @GetMapping("/add")
    public String addTaskForm(Model model) {
        model.addAttribute("task", new TaskDto());
        return "addtask";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute TaskDto taskDto) {
        taskService.createTaskFromDto(taskDto, currentUser.requireId());
        return "redirect:/task";
    }

    @GetMapping("/search")
    public String searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            Model model) {

        List<Task> results = taskService.searchTasks(currentUser.requireId(), keyword, priority, status);
        model.addAttribute("tasks", results);
        return "task";
    }

    @GetMapping("/{id}/toggle")
    public String toggleTaskComplete(@PathVariable Long id) {
        taskService.toggleComplete(id);
        return "redirect:/task";
    }
}
