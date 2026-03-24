package com.leandroftm.taskmanager.taskmanagerv2.dto;

import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        @NotBlank
        @Size(min = 1, max = 50)
        String title,
        @Size(max = 255)
        String description,
        @NotNull
        TaskPriority taskPriority,
        @NotNull
        TaskStatus taskStatus,
        @NotNull
        @Future
        LocalDateTime dueDate
) {
}
