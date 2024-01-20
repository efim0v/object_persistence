package ru.efimov.nsu.projects.objectmodel.core;

public class BeanCreatingException extends RuntimeException {

    public BeanCreatingException(String message) {
        super(message);
    }

    public BeanCreatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCreatingException(Throwable cause) {
        super(cause);
    }
}
