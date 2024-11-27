package com.testtask.taskmanagement.pojo.tasks;

import com.testtask.taskmanagement.model.TaskPriority;
import com.testtask.taskmanagement.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "DTO for task change request")
@Accessors(chain = true)
public class ChangeTaskRequest {
    @Schema(description = "A task's id", example = "1")
    @Positive(message = "Must not be less then 1.")
    @NotNull(message = "Must not be null")
    private Long id;

    @Schema(description = "A task's title", example = "Super serious task.", minLength = 10, maxLength = 60)
    @Size(min = 10, max = 60, message = "The minimum length is {min} and the maximum is {max} characters.")
    @Nullable
    private String title;

    @Schema(description = "A task's description", example = "Executor must make a tea for administrator.", minLength = 10, maxLength = 400)
    @Size(min = 10, max = 400, message = "The minimum length is {min} and the maximum is {max} characters.")
    @Nullable
    private String description;

    @Schema(description = "A task's status", example = "IN_AWAITING")
    @Nullable
    private TaskStatus status;

    @Schema(description = "A task's priority", example = "HIGH")
    @Nullable
    private TaskPriority priority;

    @Schema(description = "A task's executor", example = "user01", minLength = 5, maxLength = 20)
    @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
    @Nullable
    private String executor;
}
