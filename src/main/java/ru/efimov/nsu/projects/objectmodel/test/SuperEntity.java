package ru.efimov.nsu.projects.objectmodel.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.*;

import java.util.List;

@Entity
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class SuperEntity {
    @Id
    @GeneratedValue
    Long id;

    @OneToMany(mappedBy = "superEntity")
    final List<FirstEntity> first;

    String stringOne;
    String stringTwo;
}
