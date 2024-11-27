package com.testtask.taskmanagement.service.impl;

import com.testtask.taskmanagement.config.jwt.JwtUtils;
import com.testtask.taskmanagement.model.User;
import com.testtask.taskmanagement.model.UserRole;
import com.testtask.taskmanagement.pojo.auth.ChangeRoleRequest;
import com.testtask.taskmanagement.pojo.auth.JwtResponse;
import com.testtask.taskmanagement.pojo.auth.LoginRequest;
import com.testtask.taskmanagement.pojo.auth.RegisterRequest;
import com.testtask.taskmanagement.repository.UsersRepository;
import com.testtask.taskmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterRequest request) {
        User newUser = new User();

        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(UserRole.ROLE_USER);

        usersRepository.save(newUser);
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());

        return new JwtResponse(jwt);
    }

    @Override
    public void changeRole(ChangeRoleRequest request) {
        User existingUser = usersRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        existingUser.setRole(request.getRole());

        usersRepository.save(existingUser);
    }
}
