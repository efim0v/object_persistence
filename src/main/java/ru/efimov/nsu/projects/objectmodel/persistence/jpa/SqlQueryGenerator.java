package ru.efimov.nsu.projects.objectmodel.persistence.jpa;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata.RelationMetadata.RelationType;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata.RelationMetadata;

public class SqlQueryGenerator {

    public String generateSelectQuery(EntityMetadata metadata) {
        StringBuilder query = new StringBuilder("SELECT * FROM " + metadata.getTableName());
        // Дополнительная логика для JOIN запросов в случае отношений
        return query.toString();
    }


    public String generateInsertQuery(EntityMetadata metadata, Object entity) throws IllegalAccessException, NoSuchFieldException {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (Map.Entry<String, EntityMetadata.FieldMetadata> entry : metadata.getFields().entrySet()) {

            // Проверяем, является ли поле OneToMany, ManyToOne (для этого случая нужен отдельный запрос)
            RelationMetadata relationMetadata = entry.getValue().getRelationMetadata();
            if (relationMetadata != null) {
                continue;
            }

            Field field = entity.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            Class<?> fieldType = entry.getValue().getFieldType();
            Object value = field.get(entity);


            // Проверяем, является ли поле автогенерируемым и null
            if (entry.getValue().isGeneratedValue() && value == null) {
                continue; // Пропускаем это поле
            }

            columns.append(entry.getValue().getColumnName()).append(",");
            values.append(value == null ? "NULL" : "'" + value + "'").append(",");
        }

        // Убираем последние запятые
        if (!columns.isEmpty() && !values.isEmpty()) {
            columns.deleteCharAt(columns.length() - 1);
            values.deleteCharAt(values.length() - 1);
        }

        return "INSERT INTO " + metadata.getTableName() + " (" + columns + ") VALUES (" + values + ")";
    }

    // Методы для генерации UPDATE и DELETE запросов...
}