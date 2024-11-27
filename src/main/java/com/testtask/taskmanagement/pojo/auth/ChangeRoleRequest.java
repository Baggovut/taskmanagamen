package com.testtask.taskmanagement.pojo.auth;

import com.testtask.taskmanagement.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "DTO for user's role change request")
@Accessors(chain = true)
public class ChangeRoleRequest {
    @Schema(description = "A user's login", example = "user01", minLength = 5, maxLength = 20)
    @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String username;

    @Schema(description = "A user's role", example = "ROLE_ADMIN")
    @NotNull(message = "Must not be null")
    private UserRole role;
}
