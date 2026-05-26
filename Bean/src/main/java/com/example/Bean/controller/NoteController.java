package com.example.Bean.controller;

import com.example.Bean.model.Note;
import com.example.Bean.model.User;
import com.example.Bean.repository.UserRepository;
import com.example.Bean.security.CurrentUser;
import com.example.Bean.service.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public NoteController(NoteService noteService,
                          UserRepository userRepository,
                          CurrentUser currentUser) {
        this.noteService = noteService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String getNotes(Model model) {
        Long userId = currentUser.requireId();
        List<Note> notes = noteService.getNotesByUser(userId);
        model.addAttribute("notes", notes);
        return "notes";
    }

    @PostMapping("/add")
    public String addNote(@RequestParam("content") String content) {
        Long userId = currentUser.requireId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = new Note();
        note.setUser(user);
        note.setContent(content);
        noteService.saveNote(note);
        return "redirect:/notes";
    }

    @PostMapping("/update/{id}")
    public String updateNote(@PathVariable Long id, @RequestParam("content") String content) {
        noteService.updateNoteContent(id, content);
        return "redirect:/notes";
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        noteService.deleteNoteById(id);
        return "redirect:/notes";
    }

    @PostMapping("/pin/{id}")
    public String pinNote(@PathVariable Long id) {
        noteService.togglePin(id);
        return "redirect:/notes";
    }

    @PostMapping("/color/{id}")
    public String colorNote(@PathVariable Long id, @RequestParam("color") String color) {
        noteService.updateColor(id, color);
        return "redirect:/notes";
    }
}
