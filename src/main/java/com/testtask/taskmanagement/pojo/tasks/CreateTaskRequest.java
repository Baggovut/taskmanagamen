package com.testtask.taskmanagement.pojo.tasks;

import com.testtask.taskmanagement.model.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "DTO for creating task request")
@Accessors(chain = true)
public class CreateTaskRequest {
    @Schema(description = "A task's title", example = "Super serious task.", minLength = 10, maxLength = 60)
    @Size(min = 10, max = 60, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String title;

    @Schema(description = "A task's description", example = "Executor must make a tea for administrator.", minLength = 10, maxLength = 400)
    @Size(min = 10, max = 400, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String description;

    @Schema(description = "A task's priority", example = "HIGH")
    @NotNull(message = "Must not be null")
    private TaskPriority priority;

    @Schema(description = "A task's executor", example = "user01", minLength = 5, maxLength = 20)
    @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
    @Nullable
    private String executor;
}
