package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class EntityMetadata {
    private Class<?> entityClass;
    private String tableName;
    private Map<String, FieldMetadata> fields = new HashMap<>();

    // Конструкторы, геттеры, сеттеры...

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FieldMetadata {
        private String columnName;
        private Class<?> fieldType;
        private boolean isId;
        private RelationMetadata relationMetadata;

        // Конструкторы, геттеры, сеттеры...
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RelationMetadata {
        private RelationType relationType;
        private Class<?> targetEntity;
        private String targetTable;

        // Конструкторы, геттеры, сеттеры...

        public enum RelationType {
            ONE_TO_MANY, MANY_TO_MANY
        }
    }
}