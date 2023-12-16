package ru.efimov.nsu.projects.objectmodel;

import ru.efimov.nsu.projects.objectmodel.persistence.annotations.Entity;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.ManyToMany;

@Entity
public class TestEntity {
    Long id;
    String name;
}
