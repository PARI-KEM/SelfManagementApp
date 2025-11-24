package com.example.Bean.controller;


import com.example.Bean.model.Note;
import com.example.Bean.model.User;
import com.example.Bean.repository.UserRepository;
import com.example.Bean.service.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notes")
public class NoteController {
    private final NoteService noteService;
    private final UserRepository userRepository;
    public NoteController(NoteService noteService,UserRepository userRepository){
        this.noteService=noteService;
        this.userRepository=userRepository;
    }
    @GetMapping
    public String getNotes(Model model, Principal principal) {
        // fallback user for testing
        User user;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userRepository.findById(1L).orElseThrow(); // use test user id = 1
        }
        List<Note> notes = noteService.getNotesByUser(user.getId());
        model.addAttribute("notes", notes);
        return "notes";
    }


    @PostMapping("/add")
    public String addNote(@RequestParam("content") String content, Principal principal) {
        // fallback user for testing
        User user;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Test user not found"));
        }

        Note note = new Note();
        note.setUser(user);
        note.setContent(content);
        noteService.saveNote(note);
        return "redirect:/notes";
    }


    @PostMapping("/update/{id}")
    public String updateNote(@PathVariable Long id,@RequestParam("content") String content){
        noteService.updateNoteContent(id,content);
        return "redirect:/notes";
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id){
        noteService.deleteNoteById(id);
        return "redirect:/notes";
    }
}
