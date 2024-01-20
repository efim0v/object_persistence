package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

public interface EntityManager {
    <T> T find(Class<T> entityClass, Object primaryKey);
}
