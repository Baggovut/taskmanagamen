package com.testtask.taskmanagement.controller;

import com.testtask.taskmanagement.pojo.CommentDTO;
import com.testtask.taskmanagement.pojo.TaskDTO;
import com.testtask.taskmanagement.pojo.tasks.ChangeTaskRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateCommentRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateTaskRequest;
import com.testtask.taskmanagement.service.TasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TasksService tasksService;

    @Operation(summary = "Creating a task", tags = {"Task management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")}
    )
    @PostMapping("/create")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@RequestBody @Valid CreateTaskRequest request) {
        tasksService.createTask(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Changing a task", tags = {"Task management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User or task not found")}
    )
    @PatchMapping("/change")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<?> changeTask(@RequestBody @Valid ChangeTaskRequest request) {
        tasksService.changeTask(request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deleting a task", tags = {"Task management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Task not found")}
    )
    @DeleteMapping("/delete/{id}")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(
            @PathVariable(name = "id")
            @Valid
            @Positive(message = "Must not be less then 1.")
            @NotNull(message = "Must not be null")
            Long taskId
    ) {
        tasksService.deleteTask(taskId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Getting a list of created tasks.", tags = {"Information"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")}
    )
    @GetMapping("/owned")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<TaskDTO>> getOwnedTasks(
            @RequestParam(name = "username")
            @Valid
            @Schema(description = "Requested username", example = "user01", minLength = 5, maxLength = 20)
            @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
            @NotNull(message = "Must not be null")
            String username,

            @RequestParam(name = "page")
            @Valid
            @Schema(description = "Requested page number", example = "0")
            @Min(value = 0, message = "The minimum value is 0.")
            @Nullable
            Integer page,

            @RequestParam(name = "size")
            @Valid
            @Schema(description = "Requested size of response", example = "10", minLength = 1)
            @Positive(message = "Must not be less then 1.")
            @Nullable
            Integer size
    ) {
        List<TaskDTO> tasks = tasksService.getOwnedTasks(username, page, size);

        return ResponseEntity.ok().body(tasks);
    }

    @Operation(summary = "Getting a list of received tasks.", tags = {"Information"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")}
    )
    @GetMapping("/work")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<TaskDTO>> getWorkTasks(
            @RequestParam(name = "username")
            @Valid
            @Schema(description = "Requested username", example = "user01", minLength = 5, maxLength = 20)
            @Size(min = 5, max = 20, message = "The minimum length is {min} and the maximum is {max} characters.")
            @NotNull(message = "Must not be null")
            String username,

            @RequestParam(name = "page")
            @Valid
            @Schema(description = "Requested page number", example = "0")
            @Min(value = 0, message = "The minimum value is 0.")
            @Nullable
            Integer page,

            @RequestParam(name = "size")
            @Valid
            @Schema(description = "Requested size of response", example = "10", minLength = 1)
            @Positive(message = "Must not be less then 1.")
            @Nullable
            Integer size
    ) {
        List<TaskDTO> tasks = tasksService.getWorkTasks(username, page, size);

        return ResponseEntity.ok().body(tasks);
    }

    @Operation(summary = "Getting an info for task.", tags = {"Information"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")}
    )
    @GetMapping("/{id}/info")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable(name = "id")
            @Valid
            @Schema(description = "Requested task id", example = "10", minLength = 1)
            @Positive(message = "Must not be less then 1.")
            @NotNull(message = "Must not be null")
            Long taskId
    ) {
        TaskDTO task = tasksService.getTask(taskId);

        return ResponseEntity.ok().body(task);
    }

    @Operation(summary = "Creating a comment.", tags = {"Comments"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Task not found")}
    )
    @PostMapping("/comment")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<?> createComment(@RequestBody @Valid CreateCommentRequest request) {
        tasksService.createComment(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Getting a list of comments.", tags = {"Information"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Task not found")}
    )
    @GetMapping("/comments")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<CommentDTO>> getAllComments(
            @RequestParam(name = "id")
            @Valid
            @Schema(description = "Requested task id", example = "1")
            @Positive(message = "Must not be less then 1.")
            @NotNull(message = "Must not be null")
            Long taskId,

            @RequestParam(name = "page")
            @Valid
            @Schema(description = "Requested page number", example = "0")
            @Min(value = 0, message = "The minimum value is 0.")
            @Nullable
            Integer page,

            @RequestParam(name = "size")
            @Valid
            @Schema(description = "Requested size of response", example = "10", minLength = 1)
            @Positive(message = "Must not be less then 1.")
            @Nullable
            Integer size) {
        List<CommentDTO> comments = tasksService.getAllComments(taskId, page, size);

        return ResponseEntity.ok().body(comments);
    }
}
