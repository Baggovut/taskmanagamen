package com.testtask.taskmanagement.pojo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "DTO for JWT")
public class JwtResponse {
    @Schema(description = "JWT", example = "eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsInVzZXJuYW1lIjoidXNlcjAxIiwic3ViIjoidXNlcjAxIiwiaWF0IjoxNzMyNjM5Mjc1LCJleHAiOjE3MzI3MjU2NzV9.qqY9a9DXKHw6ddn2BMY-kRYDRBT7DRJy8xs-n2bAWkQ")
    private  String token;
}
