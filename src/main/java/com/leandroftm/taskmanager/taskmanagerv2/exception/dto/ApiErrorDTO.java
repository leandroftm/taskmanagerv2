package com.leandroftm.taskmanager.taskmanagerv2.exception.dto;

public record ApiErrorDTO(
        int status,
        String error,
        String message,
        String path
) {
}
