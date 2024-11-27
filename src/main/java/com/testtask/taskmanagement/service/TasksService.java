package com.testtask.taskmanagement.service;

import com.testtask.taskmanagement.pojo.CommentDTO;
import com.testtask.taskmanagement.pojo.TaskDTO;
import com.testtask.taskmanagement.pojo.tasks.ChangeTaskRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateCommentRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateTaskRequest;

import java.util.List;

public interface TasksService {
    void createTask(CreateTaskRequest request);

    void changeTask(ChangeTaskRequest request);

    TaskDTO getTask(Long taskId);

    void deleteTask(Long taskId);

    List<TaskDTO> getOwnedTasks(String username, Integer page, Integer size);

    List<TaskDTO> getWorkTasks(String username, Integer page, Integer size);

    void createComment(CreateCommentRequest request);

    List<CommentDTO> getAllComments(Long taskId, Integer page, Integer size);
}
