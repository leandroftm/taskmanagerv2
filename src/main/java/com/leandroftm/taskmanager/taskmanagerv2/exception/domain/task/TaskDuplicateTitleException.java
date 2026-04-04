package com.leandroftm.taskmanager.taskmanagerv2.exception.domain.task;

import com.leandroftm.taskmanager.taskmanagerv2.exception.domain.DomainException;

public class TaskDuplicateTitleException extends DomainException {
    public TaskDuplicateTitleException(String title) {

        super("The Task title " + title + " already exists");
    }
}
