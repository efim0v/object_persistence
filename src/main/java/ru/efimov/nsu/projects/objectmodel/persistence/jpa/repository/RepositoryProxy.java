package ru.efimov.nsu.projects.objectmodel.persistence.jpa.repository;

import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityManagerImpl;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityMetadata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RepositoryProxy implements InvocationHandler {
    private final EntityManagerImpl entityManager;
    private final EntityMetadata metadata;

    public RepositoryProxy(EntityManagerImpl entityManager, EntityMetadata metadata) {
        this.entityManager = entityManager;
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // Проверяем, соответствует ли имя метода шаблону запроса
        if (methodName.startsWith("findBy")) {
            String query = generateQueryFromMethodName(methodName, args);
            // Выполнение запроса
            return executeQuery(query, args);
        }

        throw new UnsupportedOperationException("Method " + methodName + " is not supported");
    }

    private String generateQueryFromMethodName(String methodName, Object[] args) {
        // Удаляем 'findBy' из имени метода
        String criteriaString = methodName.substring("findBy".length());
        // Разбиваем имя метода на отдельные критерии
        String[] criteria = criteriaString.split("And|Or|Not");

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(metadata.getTableName()).append(" WHERE ");

        for (int i = 0; i < criteria.length; i++) {
            String criterion = criteria[i];
            String columnName = convertToColumnName(criterion); // Преобразование имени критерия в имя столбца
            queryBuilder.append(columnName).append(" = ?");

            if (i < criteria.length - 1) {
                String nextPart = criteriaString.substring(criteriaString.indexOf(criteria[i]) + criteria[i].length(), criteriaString.indexOf(criteria[i + 1]));
                String logicalOperator = convertToLogicalOperator(nextPart);
                queryBuilder.append(" ").append(logicalOperator).append(" ");
            }
        }

        return queryBuilder.toString();
    }

    private String convertToColumnName(String criterion) {
        StringBuilder columnName = new StringBuilder();
        for (char ch : criterion.toCharArray()) {
            // Если символ в верхнем регистре, добавляем подчеркивание и преобразуем его в нижний регистр
            if (Character.isUpperCase(ch)) {
                if (!columnName.isEmpty()) {
                    columnName.append('_');
                }
                columnName.append(Character.toLowerCase(ch));
            } else {
                columnName.append(ch);
            }
        }
        return columnName.toString();
    }

    private String convertToLogicalOperator(String part) {
        return switch (part.trim()) {
            case "And" -> "AND";
            case "Or" -> "OR";
            case "Not" -> "NOT";
            default -> throw new IllegalArgumentException("Unsupported logical operator: " + part);
        };
    }

    private ResultSet executeQuery(String query, Object[] args) throws SQLException {
        try (Connection connection = entityManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement.executeQuery();
        }
    }
}