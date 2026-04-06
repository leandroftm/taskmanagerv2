package com.leandroftm.taskmanager.taskmanagerv2;

import com.leandroftm.taskmanager.taskmanagerv2.domain.entity.Task;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import com.leandroftm.taskmanager.taskmanagerv2.dto.CreateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.dto.TaskResponse;
import com.leandroftm.taskmanager.taskmanagerv2.dto.UpdateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskAlreadyDoneException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDueDateBeforeNowException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDuplicateTitleException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskNotFoundException;
import com.leandroftm.taskmanager.taskmanagerv2.repository.TaskRepository;
import com.leandroftm.taskmanager.taskmanagerv2.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    //.\mvnw -Dtest=TaskServiceTest test

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void shouldCreateTaskSuccessfully() {
        CreateTaskRequest createTaskRequest = createTaskRequest();

        when(taskRepository.existsByTitleIgnoreCase("Clean Room"))
                .thenReturn(false);

        when(taskRepository.save(any()))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(1L);
                    return task;
                });

        TaskResponse taskResponse = taskService.createTask(createTaskRequest);

        assertEquals(1L, taskResponse.id());
        verify(taskRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsDuplicated() {
        CreateTaskRequest createTaskRequest = createTaskRequest();

        when(taskRepository.existsByTitleIgnoreCase("Clean Room"))
                .thenReturn(true);

        assertThrows(TaskDuplicateTitleException.class,
                () ->
                        taskService.createTask(createTaskRequest)
        );
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDueDateIsBeforeNow() {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(
                "Clean Room",
                "",
                TaskPriority.HIGH,
                now.minusDays(1)
        );

        assertThrows(TaskDueDateBeforeNowException.class,
                () ->
                        taskService.createTask(createTaskRequest)
        );
    }

    @Test
    void shouldReturnTaskWhenFoundById() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse taskResponse = taskService.getById(1L);
        assertEquals("Clean", taskResponse.title());
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () ->
                        taskService.getById(1L)
        );
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        Task task = createTask();

        UpdateTaskRequest updateTaskRequest = updateTaskRequest();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.existsByTitleIgnoreCaseAndIdNot("New", 1L))
                .thenReturn(false);

        taskService.updateTask(1L, updateTaskRequest);

        assertEquals("New", task.getTitle());
    }

    @Test
    void shouldThrowExceptionWhenTaskIsAlreadyDone() {
        Task task = new Task(
                "Task",
                "",
                TaskPriority.LOW,
                TaskStatus.DONE,
                now
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest(
                "Task",
                "",
                TaskPriority.LOW,
                TaskStatus.DONE,
                now.plusDays(1)
        );

        assertThrows(TaskAlreadyDoneException.class,
                () ->
                        taskService.updateTask(1L, updateTaskRequest)
        );
    }

    @Test
    void shouldThrowExceptionWhenTitleIsDuplicatedOnUpdate() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.existsByTitleIgnoreCaseAndIdNot("New", 1L))
                .thenReturn(true);

        UpdateTaskRequest updateTaskRequest = updateTaskRequest();

        assertThrows(TaskDuplicateTitleException.class,
                () ->
                        taskService.updateTask(1L, updateTaskRequest)
        );
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTask() {
        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTask(1L));
    }

    //HELPER
    private CreateTaskRequest createTaskRequest() {
        return new CreateTaskRequest(
                "Clean Room",
                "",
                TaskPriority.HIGH,
                now.plusDays(1)
        );
    }

    private UpdateTaskRequest updateTaskRequest() {
        return new UpdateTaskRequest(
                "New",
                "",
                TaskPriority.HIGH,
                TaskStatus.TODO,
                now.plusDays(1)
        );
    }

    private Task createTask() {
        return new Task(
                "Clean",
                "",
                TaskPriority.HIGH,
                TaskStatus.TODO,
                now
        );
    }
}
