package com.leandroftm.taskmanager.taskmanagerv2.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorDTO(
        int status,
        String error,
        List<String> messages,
        String path,
        LocalDateTime timestamp
) {
}
