package com.testtask.taskmanagement.pojo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Schema(description = "DTO for user's registration request")
@Accessors(chain = true)
public class RegisterRequest {
    @Schema(description = "A user's login", example = "user01", minLength = 5, maxLength = 20)
    @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String username;

    @Schema(description = "A user's email", example = "vasya@pupkin.ru", minLength = 5, maxLength = 60)
    @Size(min = 5, max = 60, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    @Email(regexp = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$")
    private String email;

    @Schema(description = "A user's password", example = "PasD35szsXX", minLength = 8, maxLength = 30)
    @Size(min = 8, max = 30, message = "The minimum length is {min} and the maximum is {max} characters.")
    @NotNull(message = "Must not be null")
    private String password;
}
