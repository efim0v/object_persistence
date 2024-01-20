package ru.efimov.nsu.projects.objectmodel.core;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseContextImpl implements ApplicationContext {

    private final Map<String, Object> namedBeans = new ConcurrentHashMap<>();
    private final List<Object> unnamedBeans = Collections.synchronizedList(new LinkedList<>());

    private final List<BeanPostProcessor> postProcessorList = Collections.synchronizedList(new LinkedList<>());
    public void addBean(Object bean, @Nullable String beanName) {
        if (beanName != null) {
            namedBeans.put(beanName, bean);
            return;
        }
        unnamedBeans.add(bean);
    }

    // Method to get a bean by its type
    public <T> List<T> getBeanByType(Class<T> clazz) {
        List<T> fromNamedBeans = namedBeans.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();

        List<T> fromUnnamedBeans = unnamedBeans.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();

        return Stream.concat(fromNamedBeans.stream(), fromUnnamedBeans.stream())
                .collect(Collectors.toList());
    }

    public @Nullable Object getBeanByName(String beanName) {
        return namedBeans.get(beanName);
    }

    @Override
    public void addPostProcessors(Iterable<? extends BeanPostProcessor> postProcessors) {
        postProcessors.forEach(postProcessorList::add);
    }

    @Override
    public Iterable<? extends  BeanPostProcessor> getPostProcessors() {
        return postProcessorList;
    }
}