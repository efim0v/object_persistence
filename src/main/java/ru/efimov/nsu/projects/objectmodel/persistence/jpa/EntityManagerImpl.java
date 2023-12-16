package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityManagerImpl {

    private final String url;
    private final String username;
    private final String password;


    public EntityManagerImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

//    public <T> T find(Class<T> entityClass, Object primaryKey) {
//        // Логика для построения и выполнения SQL-запроса
//        // На основе класса entityClass и primaryKey
//    }

    public void persist(Object entity) {
        // Логика для преобразования объекта entity в SQL-запрос
        // для вставки или обновления данных в базе
    }

    public void remove(Object entity) {}


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
            return statement.executeUpdate();
        }
    }

    // Дополнительные методы для работы с транзакциями, параметрами запросов и т.д.
}
