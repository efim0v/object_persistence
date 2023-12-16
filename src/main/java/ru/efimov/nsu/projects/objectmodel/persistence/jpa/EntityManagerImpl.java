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
//    }

    public void persist(Object entity) {
    }

    public void remove(Object entity) {}


    public ResultSet executeQuery(String query) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            return statement.executeQuery();
        }
    }

    public int executeUpdate(String query) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            return statement.executeUpdate();
        }
    }

}
