package com.leandroftm.taskmanager.taskmanagerv2.exception.dto;

import java.time.LocalDateTime;

public record ApiErrorDTO(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
