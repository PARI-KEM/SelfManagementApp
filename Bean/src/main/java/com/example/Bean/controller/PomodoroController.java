package com.example.Bean.controller;

import com.example.Bean.enums.PomodoroType;
import com.example.Bean.model.PomodoroSession;
import com.example.Bean.service.PomodoroService;
import com.example.Bean.controller.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pomodoro")
public class PomodoroController {

    @Autowired
    private QuoteService quoteService;

    private final PomodoroService pomodoroService;

    public PomodoroController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    // -------------------- START SESSION --------------------
    @PostMapping("/start")
    @ResponseBody
    public PomodoroSession startSession(
            @RequestParam Long userId,
            @RequestParam PomodoroType type,
            @RequestParam(required = false) Long taskId
    ) {
        return pomodoroService.startSession(userId, type, taskId);
    }

    // -------------------- END SESSION --------------------
    @PostMapping("/end")
    @ResponseBody
    public PomodoroSession endSession(@RequestParam Long sessionId) {
        return pomodoroService.endSession(sessionId);
    }

    // -------------------- COUNT TODAY'S SESSIONS --------------------
    @GetMapping("/countToday")
    @ResponseBody
    public int countToday(@RequestParam Long userId) {
        return pomodoroService.countTodayPomodoros(userId);
    }

    // -------------------- POMODORO PAGE --------------------
    @GetMapping("")
    public String pomodoroPage(Model model) {

        // Thought of the day API
        String thought = quoteService.getThoughtOfTheDay();
        model.addAttribute("thoughtOfDay", thought);

        // TODO: Replace with actual logged-in user from session
        Long loggedUserId = 1L;
        model.addAttribute("userId", loggedUserId);

        return "pomodoro";
    }
}
