package com.example.Bean.service;

import com.example.Bean.model.Note;

import java.util.List;

public interface NoteService {
    int countNotes(Long userId);

    List<Note> getNotesByUser(long id);

    void saveNote(Note note);

    void deleteNoteById(Long id);

    void updateNoteContent(Long id, String content);

    void togglePin(Long id);

    void updateColor(Long id, String color);
}
