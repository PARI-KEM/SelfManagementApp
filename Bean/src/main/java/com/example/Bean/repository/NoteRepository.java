package com.example.Bean.repository;

import com.example.Bean.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    int countByUserId(Long userId);

    List<Note> findByUserIdAndIsArchivedFalse(Long userId);

    List<Note> findByUserIdOrderByPinnedDescCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId AND n.createdAt >= :start AND n.createdAt < :end")
    int countCreatedBetween(@Param("userId") Long userId,
                            @Param("start") OffsetDateTime start,
                            @Param("end") OffsetDateTime end);
}
