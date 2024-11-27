package com.testtask.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtask.taskmanagement.TaskManagementSystemApplication;
import com.testtask.taskmanagement.TestcontainersConfiguration;
import com.testtask.taskmanagement.controller.utils.TestUtils;
import com.testtask.taskmanagement.model.User;
import com.testtask.taskmanagement.model.UserRole;
import com.testtask.taskmanagement.pojo.auth.ChangeRoleRequest;
import com.testtask.taskmanagement.pojo.auth.LoginRequest;
import com.testtask.taskmanagement.pojo.auth.RegisterRequest;
import com.testtask.taskmanagement.repository.CommentsRepository;
import com.testtask.taskmanagement.repository.TasksRepository;
import com.testtask.taskmanagement.repository.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TaskManagementSystemApplication.class)
@Import(TestcontainersConfiguration.class)
@Testcontainers
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PostgreSQLContainer<?> postgres;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TasksRepository tasksRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final static int TOTAL_NUMBER_OF_PRE_CREATED_USERS = 10;
    private List<User> users;

    @BeforeEach
    void beforeEach() {
        postgres.start();
        users = TestUtils.createUsers(TOTAL_NUMBER_OF_PRE_CREATED_USERS, passwordEncoder);
        usersRepository.saveAll(users);
    }

    @AfterEach
    void afterEach() {
        commentsRepository.deleteAll();
        tasksRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    void testPostgresql() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
        }
    }

    @Test
    void registerUser_postRequest_notExistingUser_thenOk() throws Exception {
        long currentUsers = usersRepository.count();

        String nonExistingUserJsonValue = objectMapper.writeValueAsString(new RegisterRequest()
                .setUsername("userXXX")
                .setEmail("userXXXX@mail.ru")
                .setPassword("12345678")
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nonExistingUserJsonValue)
                )
                .andDo(print())
                .andExpect(status().isCreated());

        Long usersAfter = usersRepository.count();

        assertEquals(currentUsers + 1, usersAfter);
    }

    @Test
    void registerUser_postRequest_existingUser_thenConflict() throws Exception {
        long currentUsers = usersRepository.count();
        User randomExistingUser = TestUtils.getRandomNotRootAdminUser(users);

        String existingUserJsonValue = objectMapper.writeValueAsString(new RegisterRequest()
                .setUsername(randomExistingUser.getUsername())
                .setEmail(randomExistingUser.getEmail())
                .setPassword("12345678")
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existingUserJsonValue)
                )
                .andDo(print())
                .andExpect(status().isConflict());

        Long usersAfter = usersRepository.count();

        assertEquals(currentUsers, usersAfter);
    }

    @Test
    void registerUser_postRequest_notValidUserData_thenBadRequest() throws Exception {
        long currentUsers = usersRepository.count();

        String notValidUserDataJsonValue = objectMapper.writeValueAsString(new RegisterRequest()
                .setUsername("us")
                .setEmail("mail")
                .setPassword("12345678")
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidUserDataJsonValue)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Long usersAfter = usersRepository.count();

        assertEquals(currentUsers, usersAfter);
    }

    @Test
    void loginUser_postRequest_rightCredentials_thenOk() throws Exception {
        User randomExistingUser = TestUtils.getRandomNotRootAdminUser(users);

        String rightCredentialsJsonValue = objectMapper.writeValueAsString(new LoginRequest()
                .setUsername(randomExistingUser.getUsername())
                .setPassword(TestUtils.PASSWORD_STUB)
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightCredentialsJsonValue)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void loginUser_postRequest_wrongCredentials_thenUnauthorized() throws Exception {
        String wrongCredentialsJsonValue = objectMapper.writeValueAsString(new LoginRequest()
                .setUsername("user011111")
                .setPassword("password")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongCredentialsJsonValue)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

    }

    @Test
    void loginUser_postRequest_wrongUserData_thenBadRequest() throws Exception {
        String wrongUserDataJsonValue = objectMapper.writeValueAsString(new LoginRequest()
                .setUsername("us")
                .setPassword("pas")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongUserDataJsonValue)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void changeRole_postRequest_withRootAdminCredentials_thenOk() throws Exception {
        User randomExistingUser = TestUtils.getRandomPlainUser(users);

        String rootAdminUsername = users.get(TestUtils.ROOT_ADMIN_INDEX).getUsername();
        User existingUserBefore = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeRoleRequest()
                .setUsername(randomExistingUser.getUsername())
                .setRole(UserRole.ROLE_ADMIN)
        );
        assertEquals(UserRole.ROLE_USER, existingUserBefore.getRole());

        mockMvc.perform(post("/auth/changerole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(rootAdminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());

        User existingUserAfter = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();
        assertEquals(UserRole.ROLE_ADMIN, existingUserAfter.getRole());
    }

    @Test
    void changeRole_postRequest_withoutRootAdminCredentials_thenOk() throws Exception {
        User randomExistingUser = TestUtils.getRandomNotRootAdminUser(users);

        String usernameForLogin = randomExistingUser.getUsername();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeRoleRequest()
                .setUsername(randomExistingUser.getUsername())
                .setRole(UserRole.ROLE_ADMIN)
        );

        mockMvc.perform(post("/auth/changerole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(usernameForLogin, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRole_postRequest_withoutCredentials_thenUnauthorized() throws Exception {
        User randomExistingUser = TestUtils.getRandomNotRootAdminUser(users);
        User existingUserBefore = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeRoleRequest()
                .setUsername(randomExistingUser.getUsername())
                .setRole(UserRole.ROLE_ADMIN)
        );
        assertEquals(UserRole.ROLE_USER, existingUserBefore.getRole());

        mockMvc.perform(post("/auth/changerole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

        User existingUserAfter = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();
        assertEquals(UserRole.ROLE_USER, existingUserAfter.getRole());
    }

    @Test
    void changeRole_postRequest_wrongData_thenBadRequest() throws Exception {
        User randomExistingUser = TestUtils.getRandomNotRootAdminUser(users);
        String rootAdminUsername = users.get(TestUtils.ROOT_ADMIN_INDEX).getUsername();

        User existingUserBefore = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeRoleRequest()
                .setUsername(existingUserBefore.getUsername())
                .setRole(null)
        );
        assertEquals(UserRole.ROLE_USER, existingUserBefore.getRole());

        mockMvc.perform(post("/auth/changerole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(rootAdminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        User existingUserAfter = usersRepository.findByUsername(randomExistingUser.getUsername()).orElseThrow();
        assertEquals(UserRole.ROLE_USER, existingUserAfter.getRole());
    }

    @Test
    void changeRole_postRequest_nonExistingUser_thenNotFound() throws Exception {
        String rootAdminUsername = users.get(TestUtils.ROOT_ADMIN_INDEX).getUsername();
        String notExistingUsername = "userZZZZZZ";

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeRoleRequest()
                .setUsername(notExistingUsername)
                .setRole(UserRole.ROLE_ADMIN)
        );

        mockMvc.perform(post("/auth/changerole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(rootAdminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }
}
