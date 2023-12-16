package ru.efimov.nsu.projects.objectmodel.persistence.annotations;

import ru.efimov.nsu.projects.objectmodel.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {}
