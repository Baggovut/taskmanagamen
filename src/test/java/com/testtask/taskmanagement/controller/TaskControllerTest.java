package com.testtask.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtask.taskmanagement.TaskManagementSystemApplication;
import com.testtask.taskmanagement.TestcontainersConfiguration;
import com.testtask.taskmanagement.controller.utils.TestUtils;
import com.testtask.taskmanagement.model.*;
import com.testtask.taskmanagement.pojo.tasks.ChangeTaskRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateCommentRequest;
import com.testtask.taskmanagement.pojo.tasks.CreateTaskRequest;
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
import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TaskManagementSystemApplication.class)
@Import(TestcontainersConfiguration.class)
@Testcontainers
@AutoConfigureMockMvc
public class TaskControllerTest {
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
    private final static int TOTAL_NUMBER_OF_PRE_CREATED_OWNED_TASKS = 10;
    private final static int TOTAL_NUMBER_OF_PRE_CREATED_OWNED_COMMENTS_FOR_EACH_TASK = 10;
    private List<User> users;
    private List<Task> tasks;
    private User userWithWorkTasks;

    @BeforeEach
    void beforeEach() {
        postgres.start();
        users = TestUtils.createUsers(TOTAL_NUMBER_OF_PRE_CREATED_USERS, passwordEncoder);
        usersRepository.saveAll(users);

        User userWithOwnedTasks = TestUtils.getAdminUser(users);
        tasks = TestUtils.createOwnedTasks(userWithOwnedTasks, TOTAL_NUMBER_OF_PRE_CREATED_OWNED_TASKS);
        userWithWorkTasks = TestUtils.getRandomPlainUser(users);
        TestUtils.createWorkTasks(userWithWorkTasks, tasks, TOTAL_NUMBER_OF_PRE_CREATED_OWNED_TASKS / 2);
        tasksRepository.saveAll(tasks);

        List<Comment> comments = TestUtils.createComments(tasks, users, TOTAL_NUMBER_OF_PRE_CREATED_OWNED_COMMENTS_FOR_EACH_TASK);
        commentsRepository.saveAll(comments);
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
    void createTask_postRequest_withoutCredentials_thenUnauthorized() throws Exception {
        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateTaskRequest()
                .setTitle("Title for test.")
                .setDescription("Description for test.")
                .setPriority(TaskPriority.HIGH)
                .setExecutor(TestUtils.getRandomNotRootAdminUser(users).getUsername())
        );

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_postRequest_withCredentials_thenCreated() throws Exception {
        long tasksBefore = tasksRepository.count();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateTaskRequest()
                .setTitle("Title for test.")
                .setDescription("Description for test.")
                .setPriority(TaskPriority.HIGH)
                .setExecutor(TestUtils.getRandomNotRootAdminUser(users).getUsername())
        );
        String username = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore + 1, tasksAfter);
    }

    @Test
    void createTask_postRequest_withCredentials_notAdmin_thenForbidden() throws Exception {
        long tasksBefore = tasksRepository.count();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateTaskRequest()
                .setTitle("Title for test.")
                .setDescription("Description for test.")
                .setPriority(TaskPriority.HIGH)
                .setExecutor(TestUtils.getRandomNotRootAdminUser(users).getUsername())
        );

        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isForbidden());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void createTask_postRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        long tasksBefore = tasksRepository.count();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateTaskRequest()
                .setTitle("T.")
                .setDescription("D")
                .setPriority(TaskPriority.HIGH)
                .setExecutor(TestUtils.getRandomNotRootAdminUser(users).getUsername())
        );
        String username = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void createTask_postRequest_withCredentials_notExistingUser_thenNotFound() throws Exception {
        long tasksBefore = tasksRepository.count();
        String notExistedUserName = "userZZzzXXX";

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateTaskRequest()
                .setTitle("Title for test.")
                .setDescription("Description for test.")
                .setPriority(TaskPriority.HIGH)
                .setExecutor(notExistedUserName)
        );
        String username = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void deleteTask_deleteRequest_withCredentials_thenOk() throws Exception {
        long tasksBefore = tasksRepository.count();

        String username = TestUtils.getAdminUser(users).getUsername();
        long taskId = tasks.get(0).getId();

        mockMvc.perform(delete("/tasks/delete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter + 1);
    }

    @Test
    void deleteTask_deleteRequest_withCredentials_notExistingTask_thenNotFound() throws Exception {
        long tasksBefore = tasksRepository.count();

        String username = TestUtils.getAdminUser(users).getUsername();
        long notExistingTaskId = tasks.get(tasks.size() - 1).getId() + new Random().nextLong(1000, 1_000_000);

        mockMvc.perform(delete("/tasks/delete/{id}", notExistingTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void deleteTask_deleteRequest_withCredentials_notValidData_thenBadRequest() throws Exception {
        long tasksBefore = tasksRepository.count();

        String username = TestUtils.getAdminUser(users).getUsername();
        long notValidTaskId = -1;

        mockMvc.perform(delete("/tasks/delete/{id}", notValidTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void deleteTask_deleteRequest_withoutCredentials_thenUnauthorized() throws Exception {
        long tasksBefore = tasksRepository.count();
        long taskId = tasks.get(0).getId();

        mockMvc.perform(delete("/tasks/delete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void deleteTask_deleteRequest_withCredentials_notAdmin_thenForbidden() throws Exception {
        long tasksBefore = tasksRepository.count();

        String username = TestUtils.getRandomPlainUser(users).getUsername();
        long taskId = tasks.get(0).getId();

        mockMvc.perform(delete("/tasks/delete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isForbidden());

        long tasksAfter = tasksRepository.count();

        assertEquals(tasksBefore, tasksAfter);
    }

    @Test
    void getTask_getRequest_withCredentials_thenOk() throws Exception {

        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(get("/tasks/{id}/info", tasks.get(new Random().nextInt(0, tasks.size())).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void getTask_getRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        long wrongId = -1;
        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(get("/tasks/{id}/info", wrongId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void getTask_getRequest_withoutCredentials_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/tasks/{id}/info", tasks.get(new Random().nextInt(0, tasks.size())).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTask_getRequest_withCredentials_notExistingTask_thenNotFound() throws Exception {
        String username = TestUtils.getRandomPlainUser(users).getUsername();
        long wrongId = tasks.get(tasks.size() - 1).getId() + (new Random().nextLong(1000, 1_000_000));

        mockMvc.perform(get("/tasks/{id}/info", wrongId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_postRequest_withCredentials_adminUser_thenCreated() throws Exception {
        long commentsBefore = commentsRepository.count();
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateCommentRequest()
                .setTaskId(randomExistedTaskId)
                .setText("Text for comment test.")
        );
        String username = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(post("/tasks/comment", rightRequestJsonValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        long commentsAfter = commentsRepository.count();

        assertEquals(commentsBefore + 1, commentsAfter);
    }

    @Test
    void createComment_postRequest_withCredentials_workUser_thenCreated() throws Exception {
        long commentsBefore = commentsRepository.count();
        Long randomExistedWorkTaskId = TestUtils.getWorkTask(tasks, userWithWorkTasks).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateCommentRequest()
                .setTaskId(randomExistedWorkTaskId)
                .setText("Text for comment test.")
        );
        String username = userWithWorkTasks.getUsername();

        mockMvc.perform(post("/tasks/comment", rightRequestJsonValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        long commentsAfter = commentsRepository.count();

        assertEquals(commentsBefore + 1, commentsAfter);
    }

    @Test
    void createComment_postRequest_withCredentials_randomUser_thenForbidden() throws Exception {
        long commentsBefore = commentsRepository.count();
        Long taskWithoutExecutor = TestUtils.getTaskWithoutExecutor(tasks).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateCommentRequest()
                .setTaskId(taskWithoutExecutor)
                .setText("Text for comment test.")
        );
        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(post("/tasks/comment", rightRequestJsonValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isForbidden());

        long commentsAfter = commentsRepository.count();

        assertEquals(commentsBefore, commentsAfter);
    }

    @Test
    void createComment_postRequest_withoutCredentials_adminUser_thenUnauthorized() throws Exception {
        long commentsBefore = commentsRepository.count();
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new CreateCommentRequest()
                .setTaskId(randomExistedTaskId)
                .setText("Text for comment test.")
        );

        mockMvc.perform(post("/tasks/comment", rightRequestJsonValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

        long commentsAfter = commentsRepository.count();

        assertEquals(commentsBefore, commentsAfter);
    }

    @Test
    void getAllComment_getRequest_withCredentials_thenOk() throws Exception {
        Long randomTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();
        String username = TestUtils.getRandomPlainUser(users).getUsername();


        mockMvc.perform(get("/tasks/comments")
                        .param("id", randomTaskId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getAllComment_getRequest_withoutCredentials_thenUnauthorized() throws Exception {
        Long randomTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();

        mockMvc.perform(get("/tasks/comments")
                        .param("id", randomTaskId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllComment_getRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        long randomTaskId = -1L;
        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(get("/tasks/comments")
                        .param("id", Long.toString(randomTaskId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllComment_getRequest_withCredentials_notExistedTask_thenNotFound() throws Exception {
        long wrongTaskId = tasks.get(tasks.size() - 1).getId() + (new Random().nextLong(1000, 1_000_000));
        String username = TestUtils.getRandomPlainUser(users).getUsername();

        mockMvc.perform(get("/tasks/comments")
                        .param("id", Long.toString(wrongTaskId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnedTasks_getRequest_withCredentials_thenOk() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        int ownedTasks = (int) tasks.stream().filter(t -> t.getAuthor().getUsername().equals(username)).count();

        mockMvc.perform(get("/tasks/owned")
                        .param("username", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(ownedTasks)));
    }

    @Test
    void getOwnedTasks_getRequest_withoutCredentials_thenUnauthorized() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(get("/tasks/owned")
                        .param("username", username)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOwnedTasks_getRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        String wrongUsername = "us";

        mockMvc.perform(get("/tasks/owned")
                        .param("username", wrongUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnedTasks_getRequest_withCredentials_NotExistingUser_thenNotFound() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        String notExistingUsername = "user01ZZzzZss";

        mockMvc.perform(get("/tasks/owned")
                        .param("username", notExistingUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getWorkTasks_getRequest_withCredentials_thenOk() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        String workUsername = userWithWorkTasks.getUsername();
        int workTasks = (int) tasks.stream().filter(t -> Objects.equals(t.getExecutor() != null ? t.getExecutor().getUsername() : null, workUsername)).count();

        mockMvc.perform(get("/tasks/work")
                        .param("username", workUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(workTasks)));
    }

    @Test
    void getWorkTasks_getRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        String workUsername = "us";

        mockMvc.perform(get("/tasks/work")
                        .param("username", workUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWorkTasks_getRequest_withoutCredentials_thenUnauthorized() throws Exception {
        String workUsername = userWithWorkTasks.getUsername();

        mockMvc.perform(get("/tasks/work")
                        .param("username", workUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWorkTasks_getRequest_withCredentials_notExistingUser_thenNotFound() throws Exception {
        String username = TestUtils.getAdminUser(users).getUsername();
        String notExistedUsername = "userZxSSSx";

        mockMvc.perform(get("/tasks/work")
                        .param("username", notExistedUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(username, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void changeTask_patchRequest_withCredentials_adminUser_thenOk() throws Exception {
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();
        String randomExistedPlainUser = TestUtils.getRandomPlainUser(users).getUsername();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomExistedTaskId)
                .setTitle("Title for test")
                .setDescription("Description for test")
                .setStatus(TaskStatus.IN_AWAITING)
                .setPriority(TaskPriority.HIGH)
                .setExecutor(randomExistedPlainUser)
        );
        String adminUsername = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(adminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void changeTask_patchRequest_withCredentials_workUser_thenOk() throws Exception {
        Long randomWorkTaskId = TestUtils.getWorkTask(tasks, userWithWorkTasks).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomWorkTaskId)
                .setStatus(TaskStatus.IN_PROCESS)
        );
        String workUsername = userWithWorkTasks.getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(workUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void changeTask_patchRequest_withCredentials_workUser_thenForbidden() throws Exception {
        Long randomWorkTaskId = TestUtils.getWorkTask(tasks, userWithWorkTasks).getId();
        String randomExistedPlainUser = TestUtils.getRandomPlainUser(users).getUsername();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomWorkTaskId)
                .setTitle("Title for test 2")
                .setDescription("Description for test 2")
                .setStatus(TaskStatus.IN_PROCESS)
                .setPriority(TaskPriority.HIGH)
                .setExecutor(randomExistedPlainUser)
        );
        String workUsername = userWithWorkTasks.getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(workUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void changeTask_patchRequest_withoutCredentials_thenUnauthorized() throws Exception {
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();
        String randomExistedPlainUser = TestUtils.getRandomPlainUser(users).getUsername();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomExistedTaskId)
                .setTitle("Title for test")
                .setDescription("Description for test")
                .setStatus(TaskStatus.IN_AWAITING)
                .setPriority(TaskPriority.HIGH)
                .setExecutor(randomExistedPlainUser)
        );

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeTask_patchRequest_withCredentials_wrongData_thenBadRequest() throws Exception {
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomExistedTaskId)
                .setTitle("T")
                .setDescription("D")
                .setStatus(TaskStatus.IN_AWAITING)
                .setPriority(TaskPriority.HIGH)
        );
        String adminUsername = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(adminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeTask_patchRequest_withCredentials_wrongStatus_thenBadRequest() throws Exception {
        Long taskWithoutExecutor = TestUtils.getTaskWithoutExecutor(tasks).getId();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(taskWithoutExecutor)
                .setTitle("Title for test")
                .setDescription("Description for test")
                .setStatus(TaskStatus.IN_PROCESS)
                .setPriority(TaskPriority.HIGH)
        );
        String adminUsername = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(adminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeTask_patchRequest_withCredentials_notExistingUser_thenNotFound() throws Exception {
        Long randomExistedTaskId = tasks.get(new Random().nextInt(0, tasks.size())).getId();
        String notExistingUser = "userZADdaS";

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(randomExistedTaskId)
                .setTitle("Title for test")
                .setDescription("Description for test")
                .setStatus(TaskStatus.IN_AWAITING)
                .setPriority(TaskPriority.HIGH)
                .setExecutor(notExistingUser)
        );
        String adminUsername = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(adminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void changeTask_patchRequest_withCredentials_notExistingTask_thenNotFound() throws Exception {
        Long notExistingTaskId = tasks.get(tasks.size() - 1).getId() + (new Random().nextLong(1000, 1_000_000));
        String randomExistedPlainUser = TestUtils.getRandomPlainUser(users).getUsername();

        String rightRequestJsonValue = objectMapper.writeValueAsString(new ChangeTaskRequest()
                .setId(notExistingTaskId)
                .setTitle("Title for test")
                .setDescription("Description for test")
                .setStatus(TaskStatus.IN_AWAITING)
                .setPriority(TaskPriority.HIGH)
                .setExecutor(randomExistedPlainUser)
        );
        String adminUsername = TestUtils.getAdminUser(users).getUsername();

        mockMvc.perform(patch("/tasks/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightRequestJsonValue)
                        .header(TestUtils.HEADER_NAME, TestUtils.getBearer(adminUsername, mockMvc))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
