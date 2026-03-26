package com.leandroftm.taskmanager.taskmanagerv2.exception.domain;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
