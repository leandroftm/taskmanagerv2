package com.leandroftm.taskmanager.taskmanagerv2.service;

import com.leandroftm.taskmanager.taskmanagerv2.domain.entity.Task;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import com.leandroftm.taskmanager.taskmanagerv2.dto.CreateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.dto.TaskResponse;
import com.leandroftm.taskmanager.taskmanagerv2.dto.UpdateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDueDateBeforeNowException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskDuplicateTitleException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskAlreadyDoneException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task.TaskNotFoundException;
import com.leandroftm.taskmanager.taskmanagerv2.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse createTask(CreateTaskRequest taskRequest) {
        if (taskRepository.existsByTitleIgnoreCase(taskRequest.title())) {
            throw new TaskDuplicateTitleException(taskRequest.title());
        }
        if (taskRequest.dueDate().isBefore(LocalDateTime.now())) {
            throw new TaskDueDateBeforeNowException(taskRequest.dueDate());
        }
        Task task = taskRepository.save(toEntity(taskRequest));
        return new TaskResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(TaskResponse::new);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return taskRepository.findById(id).map(TaskResponse::new)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void updateTask(Long id, UpdateTaskRequest taskRequest) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getTaskStatus() == TaskStatus.DONE) {
            throw new TaskAlreadyDoneException("Task is already done");
        }

        if (taskRequest.dueDate().isBefore(LocalDateTime.now())) {
            throw new TaskDueDateBeforeNowException(taskRequest.dueDate());
        }

        if (!Objects.equals(task.getTitle(), taskRequest.title())) {
            if (taskRepository.existsByTitleIgnoreCaseAndIdNot(taskRequest.title(), id)) {
                throw new TaskDuplicateTitleException(task.getTitle());
            }
            task.setTitle(taskRequest.title());
        }

        task.update(
                taskRequest.description(),
                taskRequest.taskStatus(),
                taskRequest.taskPriority(),
                taskRequest.dueDate()
        );
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
    }

    private Task toEntity(CreateTaskRequest taskRequest) {
        return new Task(
                taskRequest.title(),
                taskRequest.description(),
                TaskStatus.TODO,
                taskRequest.taskPriority(),
                taskRequest.dueDate()
        );
    }
}
