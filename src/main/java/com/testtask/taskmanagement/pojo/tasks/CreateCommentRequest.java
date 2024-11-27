package com.testtask.taskmanagement.pojo.tasks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "DTO for creating comments request")
@Accessors(chain = true)
public class CreateCommentRequest {
    @Schema(description = "A task's id", example = "1")
    @Positive(message = "Must not be less then 1.")
    @NotNull(message = "Must not be null")
    private Long taskId;

    @Schema(description = "A task's comment", example = "Executor must make a tea for administrator.", minLength = 10, maxLength = 400)
    @Size(min = 10, max = 40, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String text;
}
