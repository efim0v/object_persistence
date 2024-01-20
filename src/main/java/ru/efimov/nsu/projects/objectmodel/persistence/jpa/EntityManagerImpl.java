package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.GeneratedValue;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.sql.*;
import java.util.Collection;
import java.util.Map;


@RequiredArgsConstructor
@Slf4j
public class EntityManagerImpl {

    private final String url;
    private final String username;
    private final String password;

    private final JpaContext context;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

//    public <T> T find(Class<T> entityClass, Object primaryKey) {
//        // Логика для построения и выполнения SQL-запроса
//        // На основе класса entityClass и primaryKey
//    }

    public void persist(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        EntityMetadata metadata = context.getMetadataByClass(entity.getClass());
        String primaryKeyField = metadata.getPrimaryKeyField();
        Field pkField = entity.getClass().getDeclaredField(primaryKeyField);
        pkField.setAccessible(true);
        Object primaryKeyValue = pkField.get(entity);

        String query;
        boolean isUpdate = primaryKeyValue != null;

        if (isUpdate) {
            query = generateUpdateQuery(metadata, entity);
        } else {
            query = new SqlQueryGenerator().generateInsertQuery(metadata, entity);
            // Вставка сущности и получение сгенерированного ключа
            long generatedKey = executeInsertAndGetGeneratedKey(query, metadata);
            if (pkField.isAnnotationPresent(GeneratedValue.class)) {
                // Установка сгенерированного ключа в поле id сущности
                pkField.set(entity, generatedKey);
            }
        }

        executeUpdate(query);

        for (Map.Entry<String, EntityMetadata.FieldMetadata> entry : metadata.getFields().entrySet()) {
            EntityMetadata.RelationMetadata relationMetadata = entry.getValue().getRelationMetadata();
            if (relationMetadata != null) {
                switch (relationMetadata.getRelationType()) {
                    case ONE_TO_ONE:
                        handleOneToOneRelation(entity, entry.getKey(), relationMetadata);
                        break;
                    case MANY_TO_ONE:
                        handleManyToOneRelation(entity, entry.getKey(), relationMetadata);
                        break;
                    case ONE_TO_MANY:
                        handleOneToManyRelation(entity, entry.getKey(), relationMetadata);
                        break;
                }
            }
        }
    }

    // Метод для выполнения запроса вставки и возврата сгенерированного ключа
    private long executeInsertAndGetGeneratedKey(String query, EntityMetadata metadata) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            log.debug("QUERY: {}", query);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(metadata.getPrimaryKeyField());
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained.");
                }
            }
        }
    }

    private void handleOneToManyRelation(Object entity, String fieldName, EntityMetadata.RelationMetadata relationMetadata) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field field = entity.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Collection<?> relatedEntities = (Collection<?>) field.get(entity);

        if (relatedEntities == null || relatedEntities.isEmpty()) {
            return; // Если нет связанных сущностей, выходим из метода
        }

        // Получаем первичный ключ родительской сущности
        EntityMetadata parentMetadata = context.getMetadataByClass(entity.getClass());
        String parentPrimaryKeyField = parentMetadata.getPrimaryKeyField();
        Field parentPkField = entity.getClass().getDeclaredField(parentPrimaryKeyField);
        parentPkField.setAccessible(true);
        Object parentPrimaryKey = parentPkField.get(entity);

        // Обновляем все связанные сущности
        for (Object relatedEntity : relatedEntities) {
            EntityMetadata relatedMetadata = context.getMetadataByClass(relatedEntity.getClass());

            // Название поля в противоположной сущности
            String relatedEntityFieldName = relationMetadata.getMappedBy();
            String relatedForeignKeyColumn = relatedMetadata.getFieldMetadata(relatedEntityFieldName).getColumnName();

            String updateQuery = "UPDATE " + relatedMetadata.getTableName() +
                    " SET " + relatedForeignKeyColumn + " = ? WHERE " + relatedMetadata.getPrimaryKeyField() + " = ?";

            Object relatedEntityPrimaryKey = getPrimaryKeyValue(relatedEntity, relatedMetadata);

            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                statement.setObject(1, parentPrimaryKey);
                statement.setObject(2, relatedEntityPrimaryKey);
                log.debug("QUERY: {}", statement);
                statement.executeUpdate();
            }
        }
    }

    private Object getPrimaryKeyValue(Object entity, EntityMetadata metadata) throws NoSuchFieldException, IllegalAccessException {
        Field primaryKeyField = entity.getClass().getDeclaredField(metadata.getPrimaryKeyField());
        primaryKeyField.setAccessible(true);
        return primaryKeyField.get(entity);
    }


    private void handleManyToOneRelation(Object entity, String fieldName, EntityMetadata.RelationMetadata relationMetadata) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field field = entity.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object relatedEntity = field.get(entity);

        if (relatedEntity == null) {
            return; // Если нет связанной сущности, выходим из метода
        }

        EntityMetadata relatedMetadata = context.getMetadataByClass(relatedEntity.getClass());
        String relatedPrimaryKeyField = relatedMetadata.getPrimaryKeyField();
        Field relatedEntityPkField = relatedEntity.getClass().getDeclaredField(relatedPrimaryKeyField);
        relatedEntityPkField.setAccessible(true);
        Object relatedEntityPrimaryKey = relatedEntityPkField.get(relatedEntity);

        // Предположим, что внешний ключ назван так же, как и поле связанной сущности
        String foreignKeyColumn = fieldName + "_id"; // Название столбца внешнего ключа

        String updateQuery = "UPDATE " + context.getMetadataByClass(entity.getClass()).getTableName() +
                " SET " + foreignKeyColumn + " = ? WHERE " + context.getMetadataByClass(entity.getClass()).getPrimaryKeyField() + " = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setObject(1, relatedEntityPrimaryKey);
            statement.setObject(2, context.getMetadataByClass(entity.getClass()).getPrimaryKeyField());
            log.debug("QUERY: {}", statement);
            statement.executeUpdate();
        }
    }

    private void handleOneToOneRelation(Object entity, String fieldName, EntityMetadata.RelationMetadata relationMetadata) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field field = entity.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object relatedEntity = field.get(entity);

        if (relatedEntity == null) {
            return; // Если нет связанной сущности, выходим из метода
        }

        EntityMetadata relatedMetadata = context.getMetadataByClass(relatedEntity.getClass());
        String relatedPrimaryKeyField = relatedMetadata.getPrimaryKeyField();
        Field relatedEntityPkField = relatedEntity.getClass().getDeclaredField(relatedPrimaryKeyField);
        relatedEntityPkField.setAccessible(true);
        Object relatedEntityPrimaryKey = relatedEntityPkField.get(relatedEntity);

        // Внешний ключ для @OneToOne обычно такой же, как и для @ManyToOne
        String foreignKeyColumn = fieldName + "_id";

        String updateQuery = "UPDATE " + context.getMetadataByClass(entity.getClass()).getTableName() +
                " SET " + foreignKeyColumn + " = ? WHERE " + context.getMetadataByClass(entity.getClass()).getPrimaryKeyField() + " = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setObject(1, relatedEntityPrimaryKey);
            statement.setObject(2, context.getMetadataByClass(entity.getClass()).getPrimaryKeyField());
            log.debug("QUERY: {}", statement);
            statement.executeUpdate();
        }
    }

    private String generateUpdateQuery(EntityMetadata metadata, Object entity) throws IllegalAccessException, NoSuchFieldException {
        // Логика для создания SQL запроса UPDATE
        StringBuilder updateQuery = new StringBuilder("UPDATE " + metadata.getTableName() + " SET ");
        String primaryKeyField = metadata.getPrimaryKeyField();
        Field pkField = entity.getClass().getDeclaredField(primaryKeyField);
        pkField.setAccessible(true);
        Object primaryKeyValue = pkField.get(entity);

        for (Map.Entry<String, EntityMetadata.FieldMetadata> entry : metadata.getFields().entrySet()) {
            if (!entry.getValue().isId()) {
                Field field = entity.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                updateQuery.append(entry.getValue().getColumnName())
                        .append(" = '")
                        .append(field.get(entity))
                        .append("', ");
            }
        }
        updateQuery.delete(updateQuery.length() - 2, updateQuery.length()); // Удаление последней запятой
        updateQuery.append(" WHERE ")
                .append(primaryKeyField)
                .append(" = ")
                .append(primaryKeyValue);
        return updateQuery.toString();
    }


    public void remove(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Class<?> entityClass = entity.getClass();
        EntityMetadata entityMetadata = context.getMetadataByClass(entityClass);

        // Рекурсивное удаление связанных сущностей
        for (Map.Entry<String, EntityMetadata.FieldMetadata> entry : entityMetadata.getFields().entrySet()) {
            EntityMetadata.RelationMetadata relationMetadata = entry.getValue().getRelationMetadata();
            if (relationMetadata != null && (relationMetadata.getRelationType() == EntityMetadata.RelationMetadata.RelationType.ONE_TO_MANY ||
                    relationMetadata.getRelationType() == EntityMetadata.RelationMetadata.RelationType.ONE_TO_ONE)) {
                handleCascadeDelete(entity, entry.getKey(), relationMetadata);
            }
        }

        // Удаление основной сущности
        String pkFieldName = entityMetadata.getPrimaryKeyField();
        Field pkField = entityClass.getDeclaredField(pkFieldName);
        pkField.setAccessible(true);
        Object id = pkField.get(entity);

        // QUERY GENERATION
        String query = "DELETE FROM " + entityMetadata.getTableName() + " WHERE " + pkFieldName + " = " + id;
        executeUpdate(query);
    }

    private void handleCascadeDelete(Object entity, String fieldName, EntityMetadata.RelationMetadata relationMetadata) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field field = entity.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        if (relationMetadata.getRelationType() == EntityMetadata.RelationMetadata.RelationType.ONE_TO_MANY) {
            Collection<?> relatedEntities = (Collection<?>) field.get(entity);
            if (relatedEntities != null) {
                for (Object relatedEntity : relatedEntities) {
                    remove(relatedEntity); // Рекурсивный вызов remove для каждой связанной сущности
                }
            }
        } else if (relationMetadata.getRelationType() == EntityMetadata.RelationMetadata.RelationType.ONE_TO_ONE) {
            Object relatedEntity = field.get(entity);
            if (relatedEntity != null) {
                remove(relatedEntity); // Рекурсивный вызов remove для связанной сущности
            }
        }
    }



    // Метод для выполнения запроса на выборку
    public ResultSet executeQuery(String query) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            return statement.executeQuery();
        }
    }

    // Метод для выполнения обновлений (INSERT, UPDATE, DELETE)
    public int executeUpdate(String query) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            log.debug("QUERY: {}", query);
            return statement.executeUpdate();
        }
    }

    // Дополнительные методы для работы с транзакциями, параметрами запросов и т.д.
}