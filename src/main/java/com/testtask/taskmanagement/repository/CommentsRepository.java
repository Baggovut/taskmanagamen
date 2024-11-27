package com.testtask.taskmanagement.repository;

import com.testtask.taskmanagement.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.task.id = ?1")
    List<Comment> findAllCommentsByTaskId(Long taskId);

    @Query("SELECT c FROM Comment c WHERE c.task.id = ?1")
    List<Comment> findAllCommentsByTaskId(Long taskId, Pageable pageable);
}
