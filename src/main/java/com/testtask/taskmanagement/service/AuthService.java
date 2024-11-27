package com.testtask.taskmanagement.service;

import com.testtask.taskmanagement.pojo.auth.ChangeRoleRequest;
import com.testtask.taskmanagement.pojo.auth.JwtResponse;
import com.testtask.taskmanagement.pojo.auth.LoginRequest;
import com.testtask.taskmanagement.pojo.auth.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    void changeRole(ChangeRoleRequest request);
}
