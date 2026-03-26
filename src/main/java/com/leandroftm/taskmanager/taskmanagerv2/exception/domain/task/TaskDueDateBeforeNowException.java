package com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;

import java.time.LocalDateTime;

public class TaskDueDateBeforeNowException extends DomainException {
    public TaskDueDateBeforeNowException(LocalDateTime dueDate) {
        super("The Task due date " + dueDate + "is before than now (" + LocalDateTime.now() + ")");
    }
}
