package com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;

public class TaskAlreadyDoneException extends DomainException {
    public TaskAlreadyDoneException(String message) {
        super(message);
    }
}
