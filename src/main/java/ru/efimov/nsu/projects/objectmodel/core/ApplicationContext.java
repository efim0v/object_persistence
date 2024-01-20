package ru.efimov.nsu.projects.objectmodel.core;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ApplicationContext {
    void addBean(Object bean, @Nullable String beanName);

    // Method to get a bean by its type
    <T> List<T> getBeanByType(Class<T> clazz);


    @Nullable Object getBeanByName(String beanName);

    void addPostProcessors(Iterable<? extends BeanPostProcessor> postProcessors);

    Iterable<? extends BeanPostProcessor> getPostProcessors();
}