package ru.efimov.nsu.projects.objectmodel.persistence.jpa;


import java.lang.reflect.Field;
import java.util.Map;

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
            columns.append(entry.getKey()).append(",");
            Field field = entity.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            values.append("'").append(field.get(entity)).append("',");
        }

        // Убираем последние запятые
        columns.deleteCharAt(columns.length() - 1);
        values.deleteCharAt(values.length() - 1);

        return "INSERT INTO " + metadata.getTableName() + " (" + columns + ") VALUES (" + values + ")";
    }

    // Методы для генерации UPDATE и DELETE запросов...
}
