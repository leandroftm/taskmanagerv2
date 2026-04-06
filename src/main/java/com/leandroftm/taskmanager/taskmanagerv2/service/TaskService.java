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
            throw new TaskDueDateBeforeNowException();
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
        Task task = findTaskById(id);

        if (task.getTaskStatus() == TaskStatus.DONE) {
            throw new TaskAlreadyDoneException(id);
        }

        if (taskRequest.dueDate().isBefore(LocalDateTime.now())) {
            throw new TaskDueDateBeforeNowException();
        }

        if (!Objects.equals(task.getTitle(), taskRequest.title())) {
            if (taskRepository.existsByTitleIgnoreCaseAndIdNot(taskRequest.title(), id)) {
                throw new TaskDuplicateTitleException(task.getTitle());
            }
            task.setTitle(taskRequest.title());
        }

        task.update(
                taskRequest.description(),
                taskRequest.taskPriority(),
                taskRequest.taskStatus(),
                taskRequest.dueDate()
        );
    }

    public void deleteTask(Long id) {
        Task task = findTaskById(id);
        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private Task toEntity(CreateTaskRequest taskRequest) {
        return new Task(
                taskRequest.title(),
                taskRequest.description(),
                taskRequest.taskPriority(),
                TaskStatus.TODO,
                taskRequest.dueDate()
        );
    }
}
