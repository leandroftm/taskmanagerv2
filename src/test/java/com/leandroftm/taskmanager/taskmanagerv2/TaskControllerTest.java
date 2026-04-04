package com.leandroftm.taskmanager.taskmanagerv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import com.leandroftm.taskmanager.taskmanagerv2.dto.CreateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.dto.TaskResponse;
import com.leandroftm.taskmanager.taskmanagerv2.dto.UpdateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskAlreadyDoneException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDuplicateTitleException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskNotFoundException;
import com.leandroftm.taskmanager.taskmanagerv2.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = "USER")
@ActiveProfiles("test")
@Transactional
public class TaskControllerTest {

    //.\mvnw -Dtest=TaskControllerTest test

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private final LocalDateTime now = LocalDateTime.now();

    //CREATE
    @Test
    void shouldReturn201AndLocationHeaderWhenCreatingTask() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "Clean room",
                "Full clean the entire room",
                TaskPriority.HIGH,
                now.plusDays(4)
        );

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(
                new TaskResponse(
                        1L,
                        "Clean room",
                        "Full clean the entire room",
                        TaskStatus.TODO,
                        TaskPriority.HIGH,
                        now,
                        now,
                        createTaskRequest.dueDate()
                )
        );

        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/tasks/1")));

        verify(taskService).createTask(argThat(task ->
                task.title().equals("Clean room") &&
                        task.taskPriority() == TaskPriority.HIGH
        ));
    }

    @Test
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "",
                "",
                TaskPriority.HIGH,
                now.plusDays(1)
        );

        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void shouldReturn400WhenDueDateIsBeforeThanNow() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "Laundry",
                "Laundry day",
                TaskPriority.LOW,
                now.minusDays(1)
        );

        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void shouldReturn400WhenTitleIsDuplicate() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "Clean bedroom",
                "Full clean the entire room",
                TaskPriority.HIGH,
                now.plusDays(4)
        );
        when(taskService.createTask(any()))
                .thenThrow(
                        new TaskDuplicateTitleException("Clean bedroom")
                );

        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("The Task title Clean bedroom already exists"));

    }

    //GET
    @Test
    void shouldReturn200WhenListAllTasks() throws Exception {
        TaskResponse taskResponse1 = new TaskResponse(
                1L,
                "Clean bedroom",
                "Full clean the entire room",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                now,
                now,
                now.plusDays(7)
        );

        TaskResponse taskResponse2 = new TaskResponse(
                2L,
                "Clean bathroom",
                "Full clean the entire room",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                now,
                now,
                now.plusDays(1)
        );

        List<TaskResponse> taskResponseList = Arrays.asList(taskResponse1, taskResponse2);
        Page<TaskResponse> taskResponsePage = new PageImpl<>(taskResponseList);

        when(taskService.getAllTasks(any(Pageable.class)))
                .thenReturn(
                        taskResponsePage
                );

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Clean bedroom"))
                .andExpect(jsonPath("$.content[1].title").value("Clean bathroom"));
    }

    @Test
    void shouldReturn200WhenTasksListIsEmpty() throws Exception {
        when(taskService.getAllTasks(any(Pageable.class)))
                .thenReturn(
                        new PageImpl<>(new ArrayList<>())
                );
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturn200WhenTaskIsFoundById() throws Exception {
        when(taskService.getById(1L)).thenReturn(
                new TaskResponse(
                        1L,
                        "Clean kitchen",
                        "Clean kitchen day",
                        TaskStatus.TODO,
                        TaskPriority.LOW,
                        now,
                        now,
                        now.plusDays(10)
                )
        );
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean kitchen"));
    }

    @Test
    void shouldReturn404WhenTaskIsNotFound() throws Exception {
        when(taskService.getById(1L)).thenThrow(
                new TaskNotFoundException(1L)
        );

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Task not found with the id " + 1L));
    }

    //UPDATE
    @Test
    void shouldReturn204WhenTaskUpdateSuccess() throws Exception {
        UpdateTaskRequest taskRequest = new UpdateTaskRequest(
                "Gym",
                "",
                TaskPriority.HIGH,
                TaskStatus.TODO,
                now.plusDays(1)
        );

        doNothing()
                .when(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));

        mockMvc.perform(put("/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNoContent());

        verify(taskService).updateTask(eq(1L), argThat(task ->
                task.title().equals("Gym") &&
                        task.taskPriority() == TaskPriority.HIGH
        ));
    }

    @Test
    void shouldReturn404WhenUpdateTaskIsNotFound() throws Exception {
        UpdateTaskRequest taskRequest = new UpdateTaskRequest(
                "Trendmil",
                "",
                TaskPriority.HIGH,
                TaskStatus.TODO,
                now.plusDays(2)
        );
        doThrow(new TaskNotFoundException(1L))
                .when(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
        mockMvc.perform(put("/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Task not found with the id " + 1L));
        verify(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
    }

    @Test
    void shouldReturn400WhenTaskIsAlreadyDone() throws Exception {
        UpdateTaskRequest taskRequest = new UpdateTaskRequest(
                "Fix Table",
                "",
                TaskPriority.LOW,
                TaskStatus.DONE,
                now.plusDays(20)
        );

        doThrow(new TaskAlreadyDoneException(1L))
                .when(taskService)
                .updateTask(eq(1L), any(UpdateTaskRequest.class));


        mockMvc.perform(put("/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Task with id " + 1L + " is already done"));
        verify(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
    }

    @Test
    void shouldReturn400WhenDueDateIsInvalid() throws Exception {
        UpdateTaskRequest taskRequest = new UpdateTaskRequest(
                "Clean garage",
                "",
                TaskPriority.LOW,
                TaskStatus.TODO,
                now.minusDays(5)
        );

        mockMvc.perform(put("/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("dueDate must be a future date"));
        verifyNoInteractions(taskService);
    }

    @Test
    void shouldReturn400WhenTitleIsInvalid() throws Exception {
        UpdateTaskRequest taskRequest = new UpdateTaskRequest(
                "Clean sidewalk",
                "",
                TaskPriority.LOW,
                TaskStatus.TODO,
                now.plusDays(5)
        );

        doThrow(new TaskDuplicateTitleException("Clean sidewalk"))
                .when(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));

        mockMvc.perform(put("/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("The Task title Clean sidewalk already exists"));

        verify(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
    }

    //DELETE
    @Test
    void shouldReturn204WhenDeleteTaskSuccess() throws Exception {
        doNothing()
                .when(taskService).deleteTask(eq(1L));

        mockMvc.perform(delete("/tasks/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(taskService).deleteTask(eq(1L));
    }

    @Test
    void shouldReturn404WhenDeleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1L))
                .when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/1")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Task not found with the id " + 1L));
        verify(taskService).deleteTask(1L);
    }
}
