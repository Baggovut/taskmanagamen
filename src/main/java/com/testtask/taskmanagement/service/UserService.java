package com.testtask.taskmanagement.service;

import com.testtask.taskmanagement.model.User;
import com.testtask.taskmanagement.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository usersRepository;

    public UserDetails getCurrentUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getCurrentUser() {
        return usersRepository.findByUsername(getCurrentUserDetails().getUsername()).orElseThrow();
    }

    public User getUserByUsername(String username) {
        return usersRepository.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
    }

    public User getUserById(Long userId) {
        return usersRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
    }

    public boolean existsByUsername(String username) {
        if (usersRepository.existsByUsername(username)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
    }
}
