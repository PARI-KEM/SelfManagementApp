package com.example.Bean.controller;

import com.example.Bean.repository.NoteRepository;
import com.example.Bean.repository.PomodoroSessionRepository;
import com.example.Bean.repository.TaskRepository;
import com.example.Bean.security.CurrentUser;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
@RequestMapping("/dashboard")
public class DashBoardController {

    private final TaskRepository taskRepo;
    private final NoteRepository noteRepo;
    private final PomodoroSessionRepository pomodoroRepo;
    private final CurrentUser currentUser;

    private static final ZoneOffset ZONE = ZoneOffset.UTC;

    public DashBoardController(TaskRepository taskRepo,
                               NoteRepository noteRepo,
                               PomodoroSessionRepository pomodoroRepo,
                               CurrentUser currentUser) {
        this.taskRepo = taskRepo;
        this.noteRepo = noteRepo;
        this.pomodoroRepo = pomodoroRepo;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String dashboard(Model model) {
        Long userId = currentUser.requireId();

        LocalDate today = LocalDate.now();
        OffsetDateTime startOfToday = today.atStartOfDay().atOffset(ZONE);
        OffsetDateTime startOfTomorrow = today.plusDays(1).atStartOfDay().atOffset(ZONE);

        int tasksCompletedToday = taskRepo.countCompletedBetween(userId, startOfToday, startOfTomorrow);
        int tasksCreatedToday   = taskRepo.countCreatedBetween(userId, startOfToday, startOfTomorrow);
        int focusSecondsToday   = pomodoroRepo.sumDurationBetween(userId, startOfToday, startOfTomorrow).intValue();
        int notesToday          = noteRepo.countCreatedBetween(userId, startOfToday, startOfTomorrow);

        model.addAttribute("tasksCompletedToday", tasksCompletedToday);
        model.addAttribute("tasksCreatedToday", tasksCreatedToday);
        model.addAttribute("focusMinutesToday", focusSecondsToday / 60);
        model.addAttribute("notesToday", notesToday);
        model.addAttribute("userId", userId);

        return "dashboard";
    }

    @GetMapping("/api/daily")
    @ResponseBody
    public Map<String, Object> daily() {
        Long userId = currentUser.requireId();
        LocalDate today = LocalDate.now();

        List<String> labels = new ArrayList<>();
        List<Integer> completedTasks = new ArrayList<>();
        List<Integer> focusMinutes = new ArrayList<>();
        List<Integer> notes = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            OffsetDateTime s = d.atStartOfDay().atOffset(ZONE);
            OffsetDateTime e = d.plusDays(1).atStartOfDay().atOffset(ZONE);
            labels.add(d.format(fmt));
            completedTasks.add(taskRepo.countCompletedBetween(userId, s, e));
            focusMinutes.add((int)(pomodoroRepo.sumDurationBetween(userId, s, e) / 60));
            notes.add(noteRepo.countCreatedBetween(userId, s, e));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("labels", labels);
        out.put("completedTasks", completedTasks);
        out.put("focusMinutes", focusMinutes);
        out.put("notes", notes);
        return out;
    }

    @GetMapping("/api/weekly")
    @ResponseBody
    public Map<String, Object> weekly() {
        Long userId = currentUser.requireId();
        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.ISO;

        List<String> labels = new ArrayList<>();
        List<Integer> completedTasks = new ArrayList<>();
        List<Integer> focusMinutes = new ArrayList<>();
        List<Integer> notes = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            LocalDate weekStart = today.minusWeeks(i).with(wf.dayOfWeek(), 1);
            LocalDate weekEnd = weekStart.plusWeeks(1);
            OffsetDateTime s = weekStart.atStartOfDay().atOffset(ZONE);
            OffsetDateTime e = weekEnd.atStartOfDay().atOffset(ZONE);
            labels.add("W" + weekStart.get(wf.weekOfWeekBasedYear()));
            completedTasks.add(taskRepo.countCompletedBetween(userId, s, e));
            focusMinutes.add((int)(pomodoroRepo.sumDurationBetween(userId, s, e) / 60));
            notes.add(noteRepo.countCreatedBetween(userId, s, e));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("labels", labels);
        out.put("completedTasks", completedTasks);
        out.put("focusMinutes", focusMinutes);
        out.put("notes", notes);
        return out;
    }

    @GetMapping("/api/monthly")
    @ResponseBody
    public Map<String, Object> monthly() {
        Long userId = currentUser.requireId();
        YearMonth thisMonth = YearMonth.now();

        List<String> labels = new ArrayList<>();
        List<Integer> completedTasks = new ArrayList<>();
        List<Integer> focusMinutes = new ArrayList<>();
        List<Integer> notes = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            YearMonth m = thisMonth.minusMonths(i);
            LocalDate first = m.atDay(1);
            LocalDate firstNext = m.plusMonths(1).atDay(1);
            OffsetDateTime s = first.atStartOfDay().atOffset(ZONE);
            OffsetDateTime e = firstNext.atStartOfDay().atOffset(ZONE);
            labels.add(first.format(fmt));
            completedTasks.add(taskRepo.countCompletedBetween(userId, s, e));
            focusMinutes.add((int)(pomodoroRepo.sumDurationBetween(userId, s, e) / 60));
            notes.add(noteRepo.countCreatedBetween(userId, s, e));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("labels", labels);
        out.put("completedTasks", completedTasks);
        out.put("focusMinutes", focusMinutes);
        out.put("notes", notes);
        return out;
    }

    @GetMapping("/report")
    public ResponseEntity<?> downloadReport(@RequestParam(defaultValue = "weekly") String range) throws Exception {
        Long userId = currentUser.requireId();

        String python = System.getenv().getOrDefault("BEAN_PYTHON", "python");
        String projectRoot = System.getProperty("user.dir");
        File script = new File(projectRoot, "analytics/report.py");
        if (!script.exists()) {
            script = new File(projectRoot, "../analytics/report.py");
        }
        if (!script.exists()) {
            return ResponseEntity.status(500)
                    .body("analytics/report.py not found. Make sure the analytics folder exists at the project root.");
        }

        File outDir = new File(System.getProperty("java.io.tmpdir"), "bean-reports");
        outDir.mkdirs();
        File outFile = new File(outDir, "bean-report-" + userId + "-" + range + "-" + System.currentTimeMillis() + ".pdf");

        ProcessBuilder pb = new ProcessBuilder(
                python, script.getAbsolutePath(),
                "--user-id", String.valueOf(userId),
                "--range", range,
                "--out", outFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String stdout = new String(p.getInputStream().readAllBytes());
        int rc = p.waitFor();
        if (rc != 0 || !outFile.exists()) {
            return ResponseEntity.status(500)
                    .body("Report generation failed (rc=" + rc + "):\n" + stdout);
        }

        FileInputStream fis = new FileInputStream(outFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + outFile.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(fis));
    }
}
