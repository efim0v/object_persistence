package ru.efimov.nsu.projects.objectmodel.persistence.jpa.repository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.efimov.nsu.projects.objectmodel.core.Autowired;
import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;
import ru.efimov.nsu.projects.objectmodel.core.BeansException;
import ru.efimov.nsu.projects.objectmodel.persistence.annotations.Repository;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityManagerImpl;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.JpaContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@NoArgsConstructor
public class RepositoryBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private EntityManagerImpl entityManager;
    @Autowired
    private JpaContext context;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAssignableFrom(Repository.class)) {
            Class<?> repositoryClass = bean.getClass();

            Type[] genericInterfaces = repositoryClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType parameterizedType) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    for (Type typeArgument : typeArguments) {
                        Class<?> typeArgClass = (Class<?>) typeArgument;
                        System.out.println("Type Argument: " + typeArgClass);
                    }
                }
            }
//            EntityMetadata metadata = context.getMetadataByClass(); // Получение метаданных сущности
//            return Proxy.newProxyInstance(
//                    bean.getClass().getClassLoader(),
//                    new Class<?>[]{bean.getClass()},
//                    new RepositoryProxy(entityManager, metadata)
//            );
        }
        return bean;
    }

    @Override
    public <T> T postProcessAfterInitialization(T bean, String beanName) throws BeansException {
        return null;
    }
}