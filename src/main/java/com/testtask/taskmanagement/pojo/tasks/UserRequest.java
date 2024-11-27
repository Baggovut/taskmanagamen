package com.testtask.taskmanagement.pojo.tasks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for user request")
public class UserRequest {
    @Schema(description = "Requested username", example = "user01", minLength = 5, maxLength = 20)
    @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String username;
}
