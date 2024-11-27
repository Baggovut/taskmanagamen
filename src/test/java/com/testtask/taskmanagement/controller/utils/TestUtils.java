package com.testtask.taskmanagement.controller.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtask.taskmanagement.model.*;
import com.testtask.taskmanagement.pojo.auth.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

final public class TestUtils {
    public static final String HEADER_NAME = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String PASSWORD_STUB = "password1234";
    public static final int ROOT_ADMIN_INDEX = 0;
    public static final int ADMIN_INDEX = 1;
    public static final String OWNED = "OWNED";
    public static final String WORK = "WORK";

    private TestUtils() {
    }

    public static List<User> createUsers(int maxUsers, PasswordEncoder passwordEncoder) {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < maxUsers; i++) {
            User newUser = new User();

            newUser.setUsername("user" + i);
            newUser.setPassword(passwordEncoder.encode(PASSWORD_STUB));
            newUser.setRole(UserRole.ROLE_USER);
            newUser.setEmail("useremail" + i + "@mail.ru");

            users.add(newUser);
        }
        users.get(ROOT_ADMIN_INDEX).setRole(UserRole.ROLE_ROOT_ADMIN);
        users.get(ADMIN_INDEX).setRole(UserRole.ROLE_ADMIN);

        return users;
    }

    public static List<Task> createOwnedTasks(User user, int maxTasks) {
        List<Task> tasks = new ArrayList<>();
        for (int currentTask = 0; currentTask < maxTasks; currentTask++) {
            Task newTask = new Task();

            newTask.setTitle("Title for test " + currentTask);
            newTask.setDescription("Description for test");
            newTask.setStatus(TaskStatus.IN_AWAITING);
            newTask.setPriority(TaskPriority.MEDIUM);
            newTask.setAuthor(user);

            tasks.add(newTask);
        }
        return tasks;
    }

    public static List<Task> createWorkTasks(User user, List<Task> tasks, int maxTasks) {
        for (int currentTask = 0; currentTask < maxTasks; currentTask++) {
            tasks.get(currentTask).setExecutor(user);
        }
        return tasks;
    }

    public static User getUserWithoutWorkTasks(List<User> users, User workUser) {
        int currentValue;
        int bound = users.size();

        do {
            currentValue = new Random().nextInt(0, bound);
        } while (!users.get(currentValue).getRole().equals(UserRole.ROLE_USER) || users.get(currentValue).getUsername().equals(workUser.getUsername()));

        return users.get(currentValue);
    }

    public static List<Comment> createComments(List<Task> tasks, List<User> users, int maxComments) {
        List<Comment> comments = new ArrayList<>();
        for (Task task : tasks) {
            for (int i = 0; i < maxComments; i++) {
                Comment newComment = new Comment();

                newComment.setText("Text for comment test." + i);
                newComment.setAuthor(getRandomPlainUser(users));
                newComment.setTask(task);

                comments.add(newComment);
            }
        }
        return comments;
    }

    public static String getToken(String response) {
        String temp = response.replace("{\"token\":\"", "");
        return temp.replace("\"}", "");
    }

    public static String getBearer(String token) {
        return BEARER_PREFIX + token;
    }

    public static String getTokenForUser(String username, MockMvc mockMvc) throws Exception {
        return getTokenForUser(username, null, mockMvc);
    }

    public static String getTokenForUser(String username, String password, MockMvc mockMvc) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String rightCredentialsJsonValue = objectMapper.writeValueAsString(new LoginRequest()
                .setUsername(username)
                .setPassword(password != null ? password : TestUtils.PASSWORD_STUB)
        );

        String tokenResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightCredentialsJsonValue)
                )
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return getToken(tokenResult);
    }

    public static String getBearer(String username, MockMvc mockMvc) throws Exception {
        return BEARER_PREFIX + getTokenForUser(username, mockMvc);
    }

    public static String getBearer(String username, String password, MockMvc mockMvc) throws Exception {
        return BEARER_PREFIX + getTokenForUser(username, password, mockMvc);
    }

    public static User getRandomNotRootAdminUser(List<User> users) {
        int currentValue;
        int bound = users.size();

        do {
            currentValue = new Random().nextInt(0, bound);
        } while (users.get(currentValue).getRole().equals(UserRole.ROLE_ROOT_ADMIN));

        return users.get(currentValue);
    }

    public static User getRandomPlainUser(List<User> users) {
        int currentValue;
        int bound = users.size();

        do {
            currentValue = new Random().nextInt(0, bound);
        } while (!users.get(currentValue).getRole().equals(UserRole.ROLE_USER));

        return users.get(currentValue);
    }

    public static User getAdminUser(List<User> users) {
        for (User user : users) {
            if (user.getRole().equals(UserRole.ROLE_ADMIN)) {
                return user;
            }
        }
        return new User();
    }

    public static Task getWorkTask(List<Task> tasks, User workUser) {
        for (Task task : tasks) {
            if (task.getExecutor() != null && task.getExecutor().getUsername().equals(workUser.getUsername())) {
                return task;
            }
        }
        return new Task();
    }

    public static Task getTaskWithoutExecutor(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getExecutor() == null) {
                return task;
            }
        }
        return new Task();
    }
}
