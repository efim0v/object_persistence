package ru.efimov.nsu.projects.objectmodel.core;

public class DuplicateBeansException extends RuntimeException {
    public DuplicateBeansException(String message) {
        super(message);
    }
}