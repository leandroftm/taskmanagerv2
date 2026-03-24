package com.leandroftm.taskmanager.taskmanagerv2.dto;

import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus taskStatus,
        TaskPriority taskPriority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dueDate
) {
}
