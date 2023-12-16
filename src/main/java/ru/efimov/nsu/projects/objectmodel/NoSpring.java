package ru.efimov.nsu.projects.objectmodel;


import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.core.ApplicationContext;
import ru.efimov.nsu.projects.objectmodel.core.BaseContextImpl;
import ru.efimov.nsu.projects.objectmodel.core.AnnotationBeanDefinitionReader;
import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityManagerImpl;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.EntityManagerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class NoSpring {
    private static final String packageToScan = "ru.efimov";
    static void runApp(Class<?> clazz, String ... args) {

        // Create application context and add context to context 👩‍🚀
        ApplicationContext context = new BaseContextImpl();
        context.addBean(ApplicationContext.class, context);

        AnnotationBeanDefinitionReader reader = new AnnotationBeanDefinitionReader();
        BeanFactory beanFactory = new BeanFactory(context);

        Set<Class<? extends BeanPostProcessor>> postProcessorsClasses = reader.findPostProcessorsClasses(packageToScan);
        Set<? extends BeanPostProcessor> instantiatedPostProcessors = postProcessorsClasses.stream()
                        .map(postProcessorClass -> {
                            try {
                                return beanFactory.createClassWithoutPostProcessing(postProcessorClass);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toSet());

        context.addPostProcessors(instantiatedPostProcessors);


        Set<Class<?>> componentsClasses = reader.findComponentClasses(packageToScan);

        List<Object> components = componentsClasses.stream()
                .map(componentClass -> {
                    try {
                        return beanFactory.createBean(componentClass);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();


        EntityManagerImpl entityManagerImpl = EntityManagerFactory.createEntityManager("application.yml");

        try {
            entityManagerImpl.executeUpdate("create table my_table (name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL);");
//            entityManager.executeUpdate("CREATE TABLE my_table(varchar)");
//            ResultSet rs0 = entityManager.executeQuery("CREATE TABLE my_table");
//            ResultSet rs1 = entityManager.executeQuery("SELECT * FROM my_table");

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Загрузите другие классы...

    }
}