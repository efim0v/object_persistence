package ru.efimov.nsu.projects.objectmodel.test;

import lombok.Builder;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.*;

import java.util.List;

@Entity
@Builder
public class TestEntity {
    @Id
    @GeneratedValue
    Long id;
    String name;

    Long phoneNumber;

//    @OneToOne
//    FirstEntity firstEntity;

//    @OneToMany
//    List<FirstEntity> firstEntityList;
}