package com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;

import java.time.LocalDateTime;

public class TaskDueDateBeforeNowException extends DomainException {
    public TaskDueDateBeforeNowException() {
        super("dueDate must be a future date");
    }
}
