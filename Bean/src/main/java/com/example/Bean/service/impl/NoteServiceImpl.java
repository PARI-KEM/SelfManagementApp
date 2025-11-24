package com.example.Bean.service.impl;

import com.example.Bean.model.Note;
import com.example.Bean.repository.NoteRepository;
import com.example.Bean.service.NoteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public int countNotes(Long userId) {
        return noteRepository.countByUserId(userId);
    }

    @Override
    public List<Note> getNotesByUser(long id) {
        return noteRepository.findByUserIdAndIsArchivedFalse(id);
    }

    @Override
    public void saveNote(Note note){
        noteRepository.save(note);
    }

    @Override
    public void deleteNoteById(Long id){
        noteRepository.deleteById(id);
    }

    @Override
    public void updateNoteContent(Long id,String content){
        Note note=noteRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Note not found with id:"+id)
        );
        note.setContent(content);
        noteRepository.save(note);
    }
}
