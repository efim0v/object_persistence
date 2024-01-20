package ru.efimov.nsu.projects.objectmodel.test;

import ru.efimov.nsu.projects.objectmodel.persistence.annotations.*;

@Entity
public class FirstEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "super_entity_id")
    private SuperEntity superEntity;

    private String string;
}
