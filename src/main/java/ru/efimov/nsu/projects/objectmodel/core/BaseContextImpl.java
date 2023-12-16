package ru.efimov.nsu.projects.objectmodel.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BaseContextImpl implements ApplicationContext {
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    // TODO: Does not support multithreading yet
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();

    // Method to add a bean to the container
    public <T> void addBean(Class<T> clazz, T beanInstance) {
        beans.put(clazz, beanInstance);
    }

    @Override
    public Iterable<? extends BeanPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    @Override
    public void addPostProcessors(Iterable<? extends BeanPostProcessor> postProcessors) {
        postProcessors.forEach(this.postProcessors::add);
    }

    // Method to get a bean by its type
    public <T> T getBeanByType(Class<T> clazz) {
        return clazz.cast(beans.get(clazz));
    }
}