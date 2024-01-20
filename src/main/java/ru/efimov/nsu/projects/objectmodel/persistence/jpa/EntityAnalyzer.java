package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import org.reflections.Reflections;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.*;

import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class EntityAnalyzer {

    public static JpaContext generateJpaContext(String basePackage) {
        Set<Class<?>> entityClasses = getEntitiesClasses(basePackage);

        Map<Class<?>, EntityMetadata> mappings = entityClasses.stream()
                .map(entityClass -> Map.entry(entityClass, analyseEntityMetadata(entityClass)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new JpaContext(mappings);
    }

    private static Set<Class<?>> getEntitiesClasses(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getTypesAnnotatedWith(Entity.class).stream()
                .filter(clazz -> !clazz.isAnnotation() && !clazz.isInterface())
                .collect(Collectors.toSet());
    }

    private static EntityMetadata analyseEntityMetadata(Class<?> entityClass) {
        String tableName = Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(entityClass.getSimpleName());

        Map<String, EntityMetadata.FieldMetadata> fieldsMetadata = Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> Map.entry(field.getName(), analyseFieldMetadata(field)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new EntityMetadata(entityClass, tableName, fieldsMetadata);
    }

    private static EntityMetadata.FieldMetadata analyseFieldMetadata(Field field) {

        if (field.isAnnotationPresent(Column.class) && field.isAnnotationPresent(JoinColumn.class)) {
            String throwMessage = String.format(
                    "There was present two annotation:" +
                            " @Column and @JoinColumn on field: %s of class %s",
                    field.getName(),
                    field.getDeclaringClass().getName());
            throw new IllegalStateException(throwMessage);
        }

        String columnName;
        if (field.isAnnotationPresent(JoinColumn.class)) {
            columnName = field.getAnnotation(JoinColumn.class).name();
        } else if (field.isAnnotationPresent(Column.class)) {
            columnName = field.getAnnotation(Column.class).name();
        } else {
            columnName = field.getName();
        }

        boolean isId = field.isAnnotationPresent(Id.class);
        boolean isGeneratedValue = field.isAnnotationPresent(GeneratedValue.class);
        EntityMetadata.RelationMetadata relationMetadata = analyseRelationMetadata(field);

        // Новая логика для обработки mappedBy
        String mappedBy = null;
        if (field.isAnnotationPresent(OneToMany.class)) {
            mappedBy = field.getAnnotation(OneToMany.class).mappedBy();
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            mappedBy = field.getAnnotation(OneToOne.class).mappedBy();
        }
        return new EntityMetadata.FieldMetadata(columnName, field.getType(), isGeneratedValue, isId, relationMetadata, mappedBy);
    }

    private static EntityMetadata.RelationMetadata analyseRelationMetadata(Field field) {
        // Обработка связей сущности
        if (
                field.isAnnotationPresent(OneToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class)
        ) {
            EntityMetadata.RelationMetadata.RelationType relationType = getRelationType(field).orElseThrow(() ->
                    new IllegalStateException("Relation type annotation missing on field: " + field.getName()));

            Class<?> targetEntity;
            String mappedBy = null;

            if (Collection.class.isAssignableFrom(field.getType())) {
                // Извлечение параметризованного типа из коллекции
                targetEntity = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                        .getActualTypeArguments()[0];
                mappedBy = getMappedByFromCollectionRelation(field);
            } else {
                targetEntity = field.getType();
                mappedBy = getMappedByFromSingleEntityRelation(field);
            }

            String targetTable = Optional.ofNullable(targetEntity.getAnnotation(Table.class))
                    .map(Table::name)
                    .orElse(targetEntity.getSimpleName());

            CascadeType[] cascadeTypes = getCascadeTypes(field);

            return RelationMetadata
                    .builder()
                    .relationType(relationType)
                    .targetEntity(targetEntity)
                    .targetTable(targetTable)
                    .mappedBy(mappedBy)
                    .cascadeTypes(cascadeTypes)
                    .build();
        }
        return null;
    }

    private static String getMappedByFromCollectionRelation(Field field) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            return field.getAnnotation(OneToMany.class).mappedBy();
        }
        return null;
    }

    private static String getMappedByFromSingleEntityRelation(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return field.getAnnotation(OneToOne.class).mappedBy();
        }
        return null;
    }

    private static CascadeType[] getCascadeTypes(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return field.getAnnotation(OneToOne.class).cascade();
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return field.getAnnotation(OneToMany.class).cascade();
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            return field.getAnnotation(ManyToOne.class).cascade();
        }
        return null;
    }

    // Метод для перевода аннотации, обозначающей тип связи в enum
    private static Optional<EntityMetadata.RelationMetadata.RelationType> getRelationType(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return Optional.of(EntityMetadata.RelationMetadata.RelationType.ONE_TO_ONE);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return Optional.of(EntityMetadata.RelationMetadata.RelationType.ONE_TO_MANY);
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            return Optional.of(EntityMetadata.RelationMetadata.RelationType.MANY_TO_ONE);
        }
        return Optional.empty();
    }
}
