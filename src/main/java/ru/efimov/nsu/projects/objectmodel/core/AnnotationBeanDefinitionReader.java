package ru.efimov.nsu.projects.objectmodel.core;

import org.reflections.Reflections;
import ru.efimov.nsu.projects.objectmodel.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class AnnotationBeanDefinitionReader implements BeanDefinitionReader {
    public Set<Class<?>> findComponentClasses(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Component.class);
        return annotatedClasses.stream().filter(clazz -> !clazz.isAnnotation() && !clazz.isInterface())
                .collect(Collectors.toSet());
    }

    public Set<Class<? extends BeanPostProcessor>> findPostProcessorsClasses(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getSubTypesOf(BeanPostProcessor.class);
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