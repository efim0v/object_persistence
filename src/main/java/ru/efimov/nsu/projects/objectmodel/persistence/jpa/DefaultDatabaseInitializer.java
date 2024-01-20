package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.Entity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultDatabaseInitializer {

    private final EntityManagerImpl entityManager;
    private final JpaContext context;

    public void initializeDatabase() throws SQLException {
        try (Connection connection = entityManager.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Шаг 1: Создание всех таблиц
            for (Map.Entry<Class<?>, EntityMetadata> entry : context.getEntityMetadataMap().entrySet()) {
                createTableIfNotExists(connection, metaData, entry.getValue());
            }

            // Шаг 2: Добавление внешних ключей
            for (Map.Entry<Class<?>, EntityMetadata> entry : context.getEntityMetadataMap().entrySet()) {
                addRelationConstraints(connection, entry.getValue());
            }
        }
    }

    private void addRelationConstraints(Connection connection, EntityMetadata entityMetadata) throws SQLException {
        // Добавление внешних ключей для каждой сущности
        for (Map.Entry<String, EntityMetadata.FieldMetadata> fieldEntry : entityMetadata.getFields().entrySet()) {
            EntityMetadata.FieldMetadata fieldMetadata = fieldEntry.getValue();
            EntityMetadata.RelationMetadata relationMetadata = fieldMetadata.getRelationMetadata();
            if (relationMetadata != null) {
                switch (relationMetadata.getRelationType()) {
                    case MANY_TO_ONE:
                        handleManyToOne(connection, entityMetadata, fieldMetadata);
                        break;
                    case MANY_TO_MANY:
                        handleManyToMany(connection, entityMetadata, fieldMetadata);
                        break;
                    // Обработка других типов связей...
                }
            }
        }
    }


    private void createTableIfNotExists(Connection connection, DatabaseMetaData metaData, EntityMetadata entityMetadata) throws SQLException {
        String tableName = entityMetadata.getTableName();
        try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
            if (!rs.next()) {
                createTable(connection, entityMetadata);
            }
        }
    }

    private void createTable(Connection connection, EntityMetadata entityMetadata) throws SQLException {
        StringBuilder createQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + entityMetadata.getTableName() + " (");

        for (Map.Entry<String, EntityMetadata.FieldMetadata> fieldEntry : entityMetadata.getFields().entrySet()) {
            EntityMetadata.FieldMetadata fieldMetadata = fieldEntry.getValue();
            Class<?> fieldType = fieldMetadata.getFieldType();

            // Пропускаем коллекции, так как они обрабатываются в контексте связей
            if (Collection.class.isAssignableFrom(fieldType)) {
                continue;
            }

            // Пропускаем сущности, но обрабатываем столбцы для внешних ключей
            if (fieldType.isAnnotationPresent(Entity.class)) {
                if (fieldMetadata.getRelationMetadata() != null) {
                    String columnName = fieldMetadata.getColumnName(); // Название столбца для внешнего ключа
                    createQuery.append(columnName).append(" BIGINT, ");
                }
                continue;
            }

            createQuery.append(fieldMetadata.getColumnName())
                    .append(" ")
                    .append(getSqlType(fieldType, fieldMetadata.isId(), fieldMetadata.isGeneratedValue()))
                    .append(fieldMetadata.isId() ? " PRIMARY KEY" : "")
                    .append(", ");
        }

        if (createQuery.lastIndexOf(",") > 0) {
            createQuery.deleteCharAt(createQuery.lastIndexOf(",")); // Удаление последней запятой
        }
        createQuery.append(")");

        try (Statement statement = connection.createStatement()) {
            log.trace("QUERY: {}", createQuery);
            statement.execute(createQuery.toString());
        }
    }

    private void handleManyToOne(Connection connection, EntityMetadata entityMetadata, EntityMetadata.FieldMetadata fieldMetadata) throws SQLException {
        // Получаем метаданные для связанной сущности
        EntityMetadata relatedEntityMetadata = context.getMetadataByClass(fieldMetadata.getRelationMetadata().getTargetEntity());

        String fkeyColumnName = fieldMetadata.getColumnName();

        // Определяем названия таблиц
        String tableName = entityMetadata.getTableName();
        String relatedTableName = relatedEntityMetadata.getTableName();
        // Создаем SQL запрос для добавления внешнего ключа
        String foreignKeyName = relatedTableName + "_fkey"; // Имя внешнего ключа
        String addForeignKeyQuery = String.format(
                "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(id)", // Предполагаем, что столбец называется 'superEntity'
                tableName,
                foreignKeyName,
                fkeyColumnName,
                relatedTableName
        );

        // Выполнение запроса
        try (Statement statement = connection.createStatement()) {
            log.debug("QUERY: {}", addForeignKeyQuery);
            statement.execute(addForeignKeyQuery);
        }
    }
    private void handleManyToMany(Connection connection, EntityMetadata entityMetadata, EntityMetadata.FieldMetadata fieldMetadata) throws SQLException {
        // Получаем метаданные для связанной сущности
        EntityMetadata relatedEntityMetadata = context.getMetadataByClass(fieldMetadata.getRelationMetadata().getTargetEntity());

        // Определяем названия таблиц
        String tableName = entityMetadata.getTableName();
        String relatedTableName = relatedEntityMetadata.getTableName();
        String joinTableName = tableName + "_" + relatedTableName;

        // Создаем SQL запрос для создания промежуточной таблицы
        StringBuilder createJoinTableQuery = new StringBuilder();
        createJoinTableQuery.append("CREATE TABLE IF NOT EXISTS ").append(joinTableName).append(" (")
                .append(tableName).append("_fkey INT, ")
                .append(relatedTableName).append("_fkey INT, ")
                .append("PRIMARY KEY (").append(tableName).append("_fkey, ").append(relatedTableName).append("_fkey), ")
                .append("FOREIGN KEY (").append(tableName).append("_fkey) REFERENCES ").append(tableName).append("(id), ")
                .append("FOREIGN KEY (").append(relatedTableName).append("_fkey) REFERENCES ").append(relatedTableName).append("(id))");

        // Выполнение запроса
        try (Statement statement = connection.createStatement()) {
            statement.execute(createJoinTableQuery.toString());
        }
    }

    private String getSqlType(Class<?> fieldType, boolean isId, boolean isGeneratedValue) {
        if (isGeneratedValue && isId) {
            return "SERIAL"; // Только для Postgres
        } else if (String.class.equals(fieldType)) {
            return "VARCHAR(255)";
        } else if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
            return "INT";
        } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
            return "BIGINT";
        } else if (double.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return "DOUBLE";
        } else if (float.class.equals(fieldType) || Float.class.equals(fieldType)) {
            return "FLOAT";
        } else if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            return "BOOLEAN";
        } else if (Date.class.equals(fieldType)) {
            return "DATE";
        } else if (LocalDate.class.equals(fieldType)) {
            return "DATE"; // Или другой SQL тип в зависимости от базы данных
        } else if (LocalDateTime.class.equals(fieldType)) {
            return "TIMESTAMP"; // Или другой SQL тип в зависимости от базы данных
        } else if (BigDecimal.class.equals(fieldType)) {
            return "DECIMAL";
        }
        // Добавьте здесь другие типы данных по мере необходимости

        throw new IllegalArgumentException("Unmapped SQL type for " + fieldType.getName());
    }
}
