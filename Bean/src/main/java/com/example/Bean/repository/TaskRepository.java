package com.example.Bean.repository;

import com.example.Bean.enums.Priority;
import com.example.Bean.enums.TaskStatus;
import com.example.Bean.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser_Id(Long userId);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND DATE(t.dueDate) = :date")
    List<Task> findTodayTasks(@Param("userId") Long userId,
                              @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND DATE(t.dueDate) = :date")
    int countByUserIdAndDueDate(@Param("userId") Long userId,
                                @Param("date") LocalDate date);

    @Query("""
           SELECT t FROM Task t
           WHERE t.user.id = :userId
           AND (:status IS NULL OR t.status = :status)
           AND (:priority IS NULL OR t.priority = :priority)
           AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    List<Task> filterTasks(@Param("userId") Long userId,
                           @Param("status") TaskStatus status,
                           @Param("priority") Priority priority,
                           @Param("keyword") String keyword);

    int countByUser_IdAndStatus(Long userId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate >= :start AND t.dueDate < :end")
    List<Task> findTodayTasks(@Param("userId") Long userId,
                              @Param("start") OffsetDateTime start,
                              @Param("end") OffsetDateTime end);

    List<Task> findByUserIdAndDueDate(Long userId, LocalDate today);

    // ---- Aggregations for dashboard reports ----

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.completed = true AND t.updatedAt >= :start AND t.updatedAt < :end")
    int countCompletedBetween(@Param("userId") Long userId,
                              @Param("start") OffsetDateTime start,
                              @Param("end") OffsetDateTime end);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.createdAt >= :start AND t.createdAt < :end")
    int countCreatedBetween(@Param("userId") Long userId,
                            @Param("start") OffsetDateTime start,
                            @Param("end") OffsetDateTime end);
}
