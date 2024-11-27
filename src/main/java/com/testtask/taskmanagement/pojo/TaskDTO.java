package com.testtask.taskmanagement.pojo;

import com.testtask.taskmanagement.model.TaskPriority;
import com.testtask.taskmanagement.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "DTO for task")
public class TaskDTO {
    @Schema(description = "A task's id", example = "1")
    private Long id;

    @Schema(description = "A task's title", example = "Super serious task.", minLength = 10, maxLength = 60)
    private String title;

    @Schema(description = "A task's description", example = "Executor must make a tea for administrator.", minLength = 10, maxLength = 400)
    private String description;

    @Schema(description = "A task's status", example = "IN_AWAITING")
    private TaskStatus status;

    @Schema(description = "A task's priority", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "A task's author id", example = "1")
    private Long authorId;

    @Schema(description = "A task's executor id", example = "1")
    private Long executorId;

    private List<CommentDTO> comments;
}
