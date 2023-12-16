package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import ru.efimov.nsu.projects.objectmodel.utils.YamlConfigReader;

import java.util.Map;

public class EntityManagerFactory {
    public static EntityManagerImpl createEntityManager(String configFileName) {
        Map<String, Object> config = YamlConfigReader.readConfig(configFileName);
        Map<String, String> dbConfig = (Map<String, String>) config.get("database");

        String url = dbConfig.get("url");
        String username = dbConfig.get("username");
        String password = dbConfig.get("password");

        EntityManagerImpl entityManagerImpl = new EntityManagerImpl(url, username, password);

        return entityManagerImpl;
    }
}