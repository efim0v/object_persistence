package ru.efimov.nsu.projects.objectmodel.core;

public interface ApplicationContext {
    void addPostProcessors(Iterable<? extends BeanPostProcessor> postProcessors);
    <T> T getBeanByType(Class<T> clazz);
    <T> void addBean(Class<T> clazz, T beanInstance);

    Iterable<? extends BeanPostProcessor> getPostProcessors();
}