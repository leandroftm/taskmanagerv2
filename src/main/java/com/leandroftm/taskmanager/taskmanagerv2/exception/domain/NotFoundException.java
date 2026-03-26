package com.leandroftm.taskmanager.taskmanagerv2.exception.domain;

public abstract class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}
