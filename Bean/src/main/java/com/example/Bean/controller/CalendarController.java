package com.example.Bean.controller;

import com.example.Bean.model.DayData;
import com.example.Bean.model.MonthData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        List<DayData> days = new ArrayList<>();
        // Example: Add 30 days
        for (int i = 1; i <= 30; i++) {
            days.add(new DayData(i, "Note for day " + i));
        }

        MonthData monthData = new MonthData(days);
        model.addAttribute("monthData", monthData);
        return "calendar";
    }
}

