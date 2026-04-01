package com.leandroftm.taskmanager.taskmanagerv2.exception;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.NotFoundException;
import com.leandroftm.taskmanager.taskmanagerv2.exception.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return buildError(HttpStatus.BAD_REQUEST, errors, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(NotFoundException ex,
                                                      HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorDTO> handleDomain(DomainException ex,
                                                    HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleException(Exception ex,
                                                       HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    // ## Build Error
    private ResponseEntity<ApiErrorDTO> buildError(HttpStatus status,
                                                   String message,
                                                   HttpServletRequest request) {
        ApiErrorDTO error = new ApiErrorDTO(
                status.value(),
                status.getReasonPhrase(),
                List.of(message),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ApiErrorDTO> buildError(HttpStatus status,
                                                   List<String> messages,
                                                   HttpServletRequest request) {
        ApiErrorDTO error = new ApiErrorDTO(
                status.value(),
                status.getReasonPhrase(),
                messages,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }
}
