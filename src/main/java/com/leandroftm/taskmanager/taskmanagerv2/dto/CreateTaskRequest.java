package com.leandroftm.taskmanager.taskmanagerv2.dto;

import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        @NotBlank
        @Size(min = 1, max = 50)
        String title,
        @Size(max = 255)
        String description,
        @NotNull
        TaskPriority taskPriority,
        @NotNull
        @Future
        LocalDateTime dueDate
) {
}
