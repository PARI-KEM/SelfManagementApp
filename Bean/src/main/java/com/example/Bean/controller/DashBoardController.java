package com.example.Bean.controller;

import com.example.Bean.model.Task;
import com.example.Bean.service.TaskService;
import com.example.Bean.service.NoteService;
import com.example.Bean.service.PomodoroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
@Controller
public class DashBoardController {

    private final TaskService taskService;
    private final NoteService noteService;
    private final PomodoroService pomodoroService;

    public DashBoardController(TaskService taskService,
                               PomodoroService pomodoroService,
                               NoteService noteService) {
        this.taskService = taskService;
        this.noteService = noteService;
        this.pomodoroService = pomodoroService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long userId = 1L;

        model.addAttribute("todayTasks", taskService.getTodayTasks(userId));
        model.addAttribute("noteCount", noteService.countNotes(userId));
        model.addAttribute("pomodoroCount", pomodoroService.countTodayPomodoros(userId));

        return "dashboard"; // Thymeleaf will resolve dashboard.html
    }

}
