package com.example.Bean.controller;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;
import com.example.Bean.security.CurrentUser;
import com.example.Bean.service.PomodoroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pomodoro")
public class PomodoroController {

    @Autowired
    private QuoteService quoteService;

    private final PomodoroService pomodoroService;
    private final CurrentUser currentUser;

    public PomodoroController(PomodoroService pomodoroService, CurrentUser currentUser) {
        this.pomodoroService = pomodoroService;
        this.currentUser = currentUser;
    }

    @PostMapping("/start")
    @ResponseBody
    public Map<String, Object> startSession(
            @RequestParam PomodoroType type,
            @RequestParam(required = false) Long taskId
    ) {
        Long userId = currentUser.requireId();
        PomodoroSession s = pomodoroService.startSession(userId, type, taskId);
        Map<String, Object> out = new HashMap<>();
        out.put("id", s.getId());
        out.put("type", s.getType());
        out.put("startTime", s.getStartTime());
        return out;
    }

    @PostMapping("/end")
    @ResponseBody
    public Map<String, Object> endSession(@RequestParam Long sessionId) {
        PomodoroSession s = pomodoroService.endSession(sessionId);
        Map<String, Object> out = new HashMap<>();
        out.put("id", s.getId());
        out.put("durationSeconds", s.getDurationSeconds());
        out.put("isCompleted", s.getIsCompleted());
        return out;
    }

    @GetMapping("/countToday")
    @ResponseBody
    public int countToday() {
        return pomodoroService.countTodayPomodoros(currentUser.requireId());
    }

    @GetMapping({"", "/"})
    public String pomodoroPage(Model model) {
        Long userId = currentUser.requireId();
        model.addAttribute("thoughtOfDay", quoteService.getThoughtOfTheDay());
        model.addAttribute("userId", userId);
        model.addAttribute("todayCount", pomodoroService.countTodayPomodoros(userId));
        return "pomodoro";
    }
}
