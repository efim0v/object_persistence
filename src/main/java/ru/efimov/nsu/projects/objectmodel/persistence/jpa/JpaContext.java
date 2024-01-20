package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class JpaContext {
    private final Map<Class<?>, EntityMetadata> entityMetadataMap;

    public EntityMetadata getMetadataByClass(Class clazz) {
        return entityMetadataMap.get(clazz);
    }
}