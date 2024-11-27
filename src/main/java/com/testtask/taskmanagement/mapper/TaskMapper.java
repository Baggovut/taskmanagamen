package com.testtask.taskmanagement.mapper;

import com.testtask.taskmanagement.model.Task;
import com.testtask.taskmanagement.pojo.TaskDTO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    private final CommentMapper commentMapper;

    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskDTO()
                .setId(task.getId())
                .setTitle(task.getTitle())
                .setDescription(task.getDescription())
                .setStatus(task.getStatus())
                .setPriority(task.getPriority())
                .setAuthorId(task.getAuthor().getId())
                .setExecutorId(task.getExecutor() != null ? task.getExecutor().getId() : null)
                .setComments(commentMapper.toCommentDTOList(task.getComments()));
    }

    public List<TaskDTO> toTaskDTOList(List<Task> tasks) {
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
