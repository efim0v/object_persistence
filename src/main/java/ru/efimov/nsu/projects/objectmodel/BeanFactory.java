package ru.efimov.nsu.projects.objectmodel;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.core.ApplicationContext;
import ru.efimov.nsu.projects.objectmodel.core.Autowired;
import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Slf4j
@Builder
public class BeanFactory {

    private ApplicationContext context;

    public <T> T createClassWithoutPostProcessing(Class<T> classToCreate) throws Exception {
        Constructor<T> noArgsConstructor = classToCreate.getDeclaredConstructor();
        return noArgsConstructor.newInstance();
    }

    public <T> T createBean(Class<T> beanClass) throws Exception {
        log.trace("arguments: {}", beanClass);

        T bean = createClassWithoutPostProcessing(beanClass);

        // Вызов BeanPostProcessor перед инициализацией
        for (BeanPostProcessor postProcessor : context.getPostProcessors()) {
            bean = postProcessor.postProcessBeforeInitialization(bean, beanClass.getName());
        }

        initializeBean(bean);

        // Вызов BeanPostProcessor после инициализации
        for (BeanPostProcessor postProcessor : context.getPostProcessors()) {
            bean = postProcessor.postProcessAfterInitialization(bean, beanClass.getName());
        }

        return bean;
    }

    // Методы для инициализации и управления жизненным циклом beans...

    public void initializeBean(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();

        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new IllegalStateException("Cannot autowire final field: " + field.getName() + " in class " + beanClass.getName());
                }

                try {
                    Class<?> dependencyClass = field.getType();
                    Object dependency = createBean(dependencyClass); // Предполагаем, что createBean уже реализован
                    field.setAccessible(true);
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to autowire field: " + field.getName() + " in class " + beanClass.getName(), e);
                }
            }
        }
    }
}