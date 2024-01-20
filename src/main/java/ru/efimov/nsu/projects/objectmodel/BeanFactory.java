package ru.efimov.nsu.projects.objectmodel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.core.ApplicationContext;
import ru.efimov.nsu.projects.objectmodel.core.Autowired;
import ru.efimov.nsu.projects.objectmodel.core.BeanCreatingException;
import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class BeanFactory {

    private final ApplicationContext context;

    private Set<Class<?>> currentlyCreatingBeans = new HashSet<>();

    private <T> T createObjectWithNoArgsConstructor(Class<T> classToCreate) {
        try {
            Constructor<T> noArgsConstructor = classToCreate.getDeclaredConstructor();
            noArgsConstructor.setAccessible(true);
            return noArgsConstructor.newInstance();
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new BeanCreatingException("There is not no args constructor to create bean" + classToCreate);
        } catch (InstantiationException instantiationException) {
            throw new BeanCreatingException("Failed to create bean " + classToCreate + ". Check to see if an attempt" +
                    " is made to instantiate an interface or abstract class.");
        } catch (Exception exception) {
            throw new IllegalStateException("There was a problem occurred while bean creating", exception);
        }
    }

    public <T> T createBean(Class<T> beanClass) {
        return createBean(beanClass, true);
    }

    public <T> T createBeanWithoutPostProcessing(Class<T> beanClass) {
        return createBean(beanClass, false);
    }

    private <T> T createBean(Class<T> beanClass, boolean postProcessingEnabled) {
        log.trace("arguments: {}", beanClass);

        if (currentlyCreatingBeans.contains(beanClass)) {
            throw new BeanCreatingException("Detected a cyclic dependency in " + beanClass.getName());
        }

        currentlyCreatingBeans.add(beanClass);

        T bean = createObjectWithNoArgsConstructor(beanClass);

        if (postProcessingEnabled) {
            for (BeanPostProcessor postProcessor : context.getPostProcessors()) {
                bean = postProcessor.postProcessBeforeInitialization(bean, beanClass.getName());
            }
        }

        initializeBean(bean);

        if (postProcessingEnabled) {
            for (BeanPostProcessor postProcessor : context.getPostProcessors()) {
                bean = postProcessor.postProcessAfterInitialization(bean, beanClass.getName());
            }
        }

        currentlyCreatingBeans.remove(beanClass);


        // TODO: handle name
        context.addBean(bean, null);
        return bean;
    }

    public void initializeBean(Object bean) {
        Class<?> beanClass = bean.getClass();

        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new BeanCreatingException("Cannot autowire final field: " + field.getName() + " in class " + beanClass.getName());
                }
                try {
                    Class<?> dependencyClass = field.getType();

                    List<?> suitableBeans = context.getBeanByType(dependencyClass);

                    Object dependency = (suitableBeans.size() == 1) ? suitableBeans.get(0) : createBean(dependencyClass);

                    field.setAccessible(true);
                    log.trace("bean: " + bean + "dependency: " + dependency);
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to autowire field: " + field.getName() + " in class " + beanClass.getName(), e);
                }
            }
        }
    }

    public Object createBeanFromMethod(Method beanMethod, Object configInstance) {
        try {
            Class<?>[] paramTypes = beanMethod.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = context.getBeanByType(paramTypes[i]).isEmpty() ?
                        createBean(paramTypes[i]) : context.getBeanByType(paramTypes[i]).get(0);
            }

            beanMethod.setAccessible(true);


            return beanMethod.invoke(configInstance, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean from method: " + beanMethod.getName(), e);
        }
    }

}