package com.testtask.taskmanagement.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice()
public class TaskManagementExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<?> handleException(SQLException exception) {
        String[] duplicateEmail = {"(email)", "already exists."};
        String duplicateEmailMessage = "This email already exists.";

        String[] duplicateUsername = {"(username)", "already exists."};
        String duplicateUsernameMessage = "This username already exists.";

        if (isContains(exception.getMessage(), duplicateEmail)) {
            return new ResponseEntity<>(duplicateEmailMessage, HttpStatus.CONFLICT);
        } else if (isContains(exception.getMessage(), duplicateUsername)) {
            return new ResponseEntity<>(duplicateUsernameMessage, HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getAllErrors().forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(errors, exception.getStatusCode());
    }

    private boolean isContains(String message, String[] words) {
        return Arrays.stream(words).allMatch(message::contains);
    }
}
