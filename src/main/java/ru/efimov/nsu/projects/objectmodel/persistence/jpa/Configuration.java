package ru.efimov.nsu.projects.objectmodel.persistence.jpa;

import ru.efimov.nsu.projects.objectmodel.core.Bean;
import ru.efimov.nsu.projects.objectmodel.core.Component;

@Component
public class Configuration {
    @Bean
    public JpaContext getContext() {
        return EntityAnalyzer.generateJpaContext("ru.efimov.nsu.projects.objectmodel");
    }

    @Bean
    public EntityManagerImpl getEntityManager() {
        return EntityManagerFactory.createEntityManager("config.yaml", getContext());
    }
}