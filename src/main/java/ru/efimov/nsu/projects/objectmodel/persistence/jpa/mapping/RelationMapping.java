package ru.efimov.nsu.projects.objectmodel.persistence.jpa.mapping;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationMapping {
    private String fieldName;
    private Class<?> targetEntity;
    private RelationType relationType;

    public enum RelationType {
        ONE_TO_MANY, MANY_TO_MANY
    }
}