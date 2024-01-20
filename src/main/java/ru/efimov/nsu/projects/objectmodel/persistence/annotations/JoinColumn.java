package ru.efimov.nsu.projects.objectmodel.persistence.annotations;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
    String name();
}