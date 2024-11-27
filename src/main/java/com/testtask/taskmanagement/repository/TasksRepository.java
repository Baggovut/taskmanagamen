package com.testtask.taskmanagement.repository;

import com.testtask.taskmanagement.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TasksRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.author = (SELECT u FROM User u WHERE u.username = ?1)")
    List<Task> findAllByAuthorUsername(String username);

    @Query("SELECT t FROM Task t WHERE t.author = (SELECT u FROM User u WHERE u.username = ?1)")
    List<Task> findAllByAuthorUsername(String username, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.executor = (SELECT u FROM User u WHERE u.username = ?1)")
    List<Task> findAllByExecutorUsername(String username);

    @Query("SELECT t FROM Task t WHERE t.executor = (SELECT u FROM User u WHERE u.username = ?1)")
    List<Task> findAllByExecutorUsername(String username, Pageable pageable);
}
