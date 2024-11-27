package com.testtask.taskmanagement.service.impl;

import com.testtask.taskmanagement.mapper.CommentMapper;
import com.testtask.taskmanagement.mapper.TaskMapper;
import com.testtask.taskmanagement.model.Comment;
import com.testtask.taskmanagement.model.Task;
import com.testtask.taskmanagement.model.TaskStatus;
import com.testtask.taskmanagement.model.UserRole;
import com.testtask.taskmanagement.pojo.CommentDTO;
import com.testtask.taskmanagement.pojo.TaskDTO;
import com.testtask.taskmanagement.pojo.tasks.ChangeTaskRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateCommentRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateTaskRequest;
import com.testtask.taskmanagement.repository.CommentsRepository;
import com.testtask.taskmanagement.repository.TasksRepository;
import com.testtask.taskmanagement.service.TasksService;
import com.testtask.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TasksServiceImpl implements TasksService {
    private final TasksRepository tasksRepository;
    private final CommentsRepository commentsRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final CommentMapper commentMapper;

    @Override
    public void createTask(CreateTaskRequest request) {
        Task newTask = new Task();

        newTask.setAuthor(userService.getCurrentUser());
        newTask.setTitle(request.getTitle());
        newTask.setDescription(request.getDescription());
        newTask.setPriority(request.getPriority());
        newTask.setStatus(TaskStatus.IN_AWAITING);
        if (request.getExecutor() != null) {
            newTask.setExecutor(userService.getUserByUsername(request.getExecutor()));
        }

        tasksRepository.save(newTask);
    }

    @Override
    public void changeTask(ChangeTaskRequest request) {
        Task existingTask = getTaskById(request.getId());

        if (request.getTitle() != null && hasAdminPermission()) {
            existingTask.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && hasAdminPermission()) {
            existingTask.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && hasPermission(existingTask)) {
            if (existingTask.getExecutor() == null && request.getExecutor() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't change task's status. Executor not found.");
            } else {
                existingTask.setStatus(request.getStatus());
            }
        }

        if (request.getPriority() != null && hasAdminPermission()) {
            existingTask.setPriority(request.getPriority());
        }

        if (request.getExecutor() != null && hasAdminPermission()) {
            existingTask.setExecutor(userService.getUserByUsername(request.getExecutor()));
        }

        tasksRepository.save(existingTask);
    }

    @Override
    public TaskDTO getTask(Long taskId) {
        return taskMapper.toDTO(getTaskById(taskId));
    }

    @Override
    public void deleteTask(Long taskId) {
        if (tasksRepository.existsById(taskId)) {
            tasksRepository.deleteById(taskId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
        }
    }

    @Override
    public List<TaskDTO> getOwnedTasks(String username, Integer page, Integer size) {
        if (userService.existsByUsername(username)) {
            if (page == null || size == null) {
                return taskMapper.toTaskDTOList(tasksRepository.findAllByAuthorUsername(username));
            } else {
                Pageable pageable = PageRequest.of(page, size);
                return taskMapper.toTaskDTOList(tasksRepository.findAllByAuthorUsername(username, pageable));
            }
        }
        return List.of();
    }

    @Override
    public List<TaskDTO> getWorkTasks(String username, Integer page, Integer size) {
        if (userService.existsByUsername(username)) {
            if (page == null || size == null) {
                return taskMapper.toTaskDTOList(tasksRepository.findAllByExecutorUsername(username));
            } else {
                Pageable pageable = PageRequest.of(page, size);
                return taskMapper.toTaskDTOList(tasksRepository.findAllByExecutorUsername(username, pageable));
            }
        }
        return List.of();
    }

    @Override
    public void createComment(CreateCommentRequest request) {
        Task existingTask = getTaskById(request.getTaskId());

        if (hasPermission(existingTask)) {
            Comment newComment = new Comment();

            newComment.setText(request.getText());
            newComment.setTask(getTaskById(request.getTaskId()));
            newComment.setAuthor(userService.getCurrentUser());

            commentsRepository.save(newComment);
        }
    }

    @Override
    public List<CommentDTO> getAllComments(Long taskId, Integer page, Integer size) {
        if (isExistsById(taskId)) {
            if (page == null || size == null) {
                return commentMapper.toCommentDTOList(commentsRepository.findAllCommentsByTaskId(taskId));
            } else {
                Pageable pageable = PageRequest.of(page, size);
                return commentMapper.toCommentDTOList(commentsRepository.findAllCommentsByTaskId(taskId, pageable));
            }
        }
        return List.of();
    }

    private boolean hasPermission(Task currentTask) {
        if (Objects.equals(currentTask.getExecutor() != null ? currentTask.getExecutor().getUsername() : null, userService.getCurrentUser().getUsername()) || hasAdminPermission()) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can't change task. Permission denied.");
        }
    }

    private boolean hasAdminPermission() {
        if (userService.getCurrentUser().getRole().equals(UserRole.ROLE_ADMIN)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can't change task. Permission denied.");
        }
    }

    private boolean isExistsById(Long taskId) {
        if (!tasksRepository.existsById(taskId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
        }
        return true;
    }

    private Task getTaskById(Long taskId) {
        return tasksRepository.findById(taskId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.")
        );
    }
}
