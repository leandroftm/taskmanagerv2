package com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;

public class TaskAlreadyDoneException extends DomainException {
    public TaskAlreadyDoneException(Long id) {
        super("Task with id" + id + " is already done");
    }
}
