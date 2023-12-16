package ru.efimov.nsu.projects.objectmodel.persistence.jpa.mapping;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMapping {
    private Class<?> entityType;
    private Map<String, RelationMapping> relations = new HashMap<>();
}