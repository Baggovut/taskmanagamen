package com.testtask.taskmanagement.controller;

import com.testtask.taskmanagement.pojo.auth.ChangeRoleRequest;
import com.testtask.taskmanagement.pojo.auth.JwtResponse;
import com.testtask.taskmanagement.pojo.auth.LoginRequest;
import com.testtask.taskmanagement.pojo.auth.RegisterRequest;
import com.testtask.taskmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "User registration", tags = {"Registration"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists.")}
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "User authentication", tags = {"Authorization"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}
    )
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
        JwtResponse response = authService.login(request);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Change user's role", tags = {"Authorization"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "User not found")}
    )
    @PostMapping("/changerole")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<?> changeRole(@RequestBody @Valid ChangeRoleRequest request) {
        authService.changeRole(request);

        return ResponseEntity.ok().build();
    }
}
