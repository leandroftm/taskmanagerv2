package com.leandroftm.taskmanager.taskmanagerv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import com.leandroftm.taskmanager.taskmanagerv2.dto.CreateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.dto.TaskResponse;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDuplicateTitleException;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        verify(taskService).createTask(any());
    }

    @Test
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "",
                "",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1)
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
                .andExpect(jsonPath("$.messages")
                        .value("The Task title \"Clean bedroom\" already exists"));

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

    /*TODO




    //get empty list return 200

    //get by id return 200 -> /tasks/1

    //get task not found return 404
    //when(taskService.getById(IL)).thenThrow(new TaskNotFoundException(1L));

    //UPDATE
    //update success 204

    //update task not found 404

    //update task already done 400

    //update invalid due date 400

    //update invalid title 400

    //DELETE
    //delete success 204

    //delete task not found 404
     */

}
