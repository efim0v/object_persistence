package ru.efimov.nsu.projects.objectmodel.persistence.jpa;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata.RelationMetadata.RelationType;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata.RelationMetadata;

public class SqlQueryGenerator {

    public String generateSelectQuery(EntityMetadata metadata) {
        StringBuilder query = new StringBuilder("SELECT * FROM " + metadata.getTableName());
        return query.toString();
    }


    public String generateInsertQuery(EntityMetadata metadata, Object entity) throws IllegalAccessException, NoSuchFieldException {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (Map.Entry<String, EntityMetadata.FieldMetadata> entry : metadata.getFields().entrySet()) {

            RelationMetadata relationMetadata = entry.getValue().getRelationMetadata();
            if (relationMetadata != null) {
                continue;
            }

            Field field = entity.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            Class<?> fieldType = entry.getValue().getFieldType();
            Object value = field.get(entity);


            if (entry.getValue().isGeneratedValue() && value == null) {
                continue;
            }

            columns.append(entry.getValue().getColumnName()).append(",");
            values.append(value == null ? "NULL" : "'" + value + "'").append(",");
        }

        if (!columns.isEmpty() && !values.isEmpty()) {
            columns.deleteCharAt(columns.length() - 1);
            values.deleteCharAt(values.length() - 1);
        }

        return "INSERT INTO " + metadata.getTableName() + " (" + columns + ") VALUES (" + values + ")";
    }

}