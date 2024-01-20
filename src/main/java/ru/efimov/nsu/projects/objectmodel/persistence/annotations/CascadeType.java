package ru.efimov.nsu.projects.objectmodel.persistence.annotations;

public enum CascadeType {
    ALL,
    PERSIST,
    REMOVE,
    UPDATE;
    private CascadeType() {
    }
}