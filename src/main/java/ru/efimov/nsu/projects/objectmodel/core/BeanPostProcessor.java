package ru.efimov.nsu.projects.objectmodel.core;

public interface BeanPostProcessor {
    <T> T postProcessBeforeInitialization(T bean, String beanName) throws BeansException;

     <T> T postProcessAfterInitialization(T bean, String beanName) throws BeansException;
}