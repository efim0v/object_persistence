package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.CascadeType;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class EntityMetadata {
    private Class<?> entityClass;
    private String tableName;
    private Map<String, FieldMetadata> fields = new HashMap<>();

    String getPrimaryKeyField() {
        List<String> ids = fields.entrySet().stream()
                .filter(fieldNameAndMetadata -> fieldNameAndMetadata.getValue().isId())
                .map(Map.Entry::getKey)
                .toList();
        if (ids.size() != 1) {
            throw new IllegalStateException("Entity must have exactly one primary key." +
                    " Check the @Id annotations in your " + entityClass + " entity. Declared ID fields are: " + ids + ".");
        }
        return ids.get(0);
    }

    FieldMetadata getFieldMetadata(String fieldName) {
        return fields.get(fieldName);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FieldMetadata {
        private String columnName;
        private Class<?> fieldType;
        private boolean isGeneratedValue; // Сейчас добавлено для ID
        private boolean isId;
        private RelationMetadata relationMetadata;
        private String mappedBy; // Добавлено для @OneToMany, @ManyToMany, @OneToOne
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class RelationMetadata {
        private RelationType relationType;
        private Class<?> targetEntity;
        private String targetTable;
        private String mappedBy; // Для обратной стороны связи
        private CascadeType[] cascadeTypes;

        public enum RelationType {
            ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY, MANY_TO_ONE
        }
    }
}