package com.example.Bean.repository;

import com.example.Bean.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    int countByUserId(Long userId);

    List<Note> findByUserIdAndIsArchivedFalse(Long userId);

    List<Note> findByUserIdOrderByPinnedDescCreatedAtDesc(Long userId);


}
