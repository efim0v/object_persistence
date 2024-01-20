package ru.efimov.nsu.projects.objectmodel.core;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class AnnotationBeanDefinitionReader implements BeanDefinitionReader {
    public Set<Class<?>> findComponentClasses(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Component.class);
        return annotatedClasses.stream().filter(clazz -> !clazz.isAnnotation() && !clazz.isInterface())
                .collect(Collectors.toSet());
    }

    public Set<Class<? extends BeanPostProcessor>> findPostProcessorsClasses(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getSubTypesOf(BeanPostProcessor.class);
    }

    public Set<Method> findBeanMethods(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(Component.class).stream()
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .collect(Collectors.toSet());
    }

    public List<Class<?>> findComponentClasses(Class<?>... classesToScan) {
        List<Class<?>> componentClasses = new ArrayList<>();
        for (Class<?> clazz : classesToScan) {
            if (clazz.isAnnotationPresent(Component.class)) {
                componentClasses.add(clazz);
            }
        }
        return componentClasses;
    }
}