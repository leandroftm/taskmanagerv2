package com.leandroftm.taskmanager.taskmanagerv2.dto;

import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotNull
        @Size(min = 1, max = 50)
        String title,
        @Size(max = 255)
        String description,
        @NotBlank
        @Size(min = 1, max = 25)
        TaskPriority taskPriority,
        @NotBlank
        @Size(min = 1, max = 25)
        TaskStatus taskStatus,
        @NotNull
        LocalDateTime dueDate
) {
}
