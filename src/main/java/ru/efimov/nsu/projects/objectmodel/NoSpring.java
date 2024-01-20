package ru.efimov.nsu.projects.objectmodel;


import lombok.extern.slf4j.Slf4j;
import ru.efimov.nsu.projects.objectmodel.core.ApplicationContext;
import ru.efimov.nsu.projects.objectmodel.core.BaseContextImpl;
import ru.efimov.nsu.projects.objectmodel.core.AnnotationBeanDefinitionReader;
import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;
import ru.efimov.nsu.projects.objectmodel.persistence.jpa.*;
import ru.efimov.nsu.projects.objectmodel.test.FirstEntity;
import ru.efimov.nsu.projects.objectmodel.test.SuperEntity;
import ru.efimov.nsu.projects.objectmodel.test.TestEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class NoSpring {
    private static final String packageToScan = "ru.efimov";

    private static final String dropAllTables = "DO $$\n" +
            "DECLARE\n" +
            "    r RECORD;\n" +
            "BEGIN\n" +
            "    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP\n" +
            "        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';\n" +
            "    END LOOP;\n" +
            "END $$;";

    static void runApp(Class<?> clazz, String... args) {

        // Create application context and add context to context 👩‍🚀
        ApplicationContext context = new BaseContextImpl();
        context.addBean(context, null);

        AnnotationBeanDefinitionReader reader = new AnnotationBeanDefinitionReader();
        BeanFactory beanFactory = new BeanFactory(context);

        Set<Class<? extends BeanPostProcessor>> postProcessorsClasses = reader.findPostProcessorsClasses(packageToScan);
        Set<? extends BeanPostProcessor> instantiatedPostProcessors = postProcessorsClasses.stream()
                        .map(postProcessorClass -> {
                            try {
                                return beanFactory.createBeanWithoutPostProcessing(postProcessorClass);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toSet());



        Set<Class<?>> componentsClasses = reader.findComponentClasses(packageToScan);

        componentsClasses
                .forEach(component -> beanFactory.createBean(component));


                reader.findBeanMethods(packageToScan)
                .stream().map(method -> beanFactory.createBeanFromMethod(method, context.getBeanByType(method.getDeclaringClass()).get(0)))
                        .forEach(bean -> context.addBean(bean, null));

                JpaContext jpaContext = context.getBeanByType(JpaContext.class).get(0);

        EntityManagerImpl entityManagerImpl = EntityManagerFactory.createEntityManager("application.yml", jpaContext);

        try {
            // Удаляем старые таблицы
            entityManagerImpl.executeUpdate(dropAllTables);

            //Инициализация баз данных
            DefaultDatabaseInitializer initializer = new DefaultDatabaseInitializer(entityManagerImpl, jpaContext);
            initializer.initializeDatabase();

//            entityManagerImpl.executeUpdate("create table my_table (name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL);");
            TestEntity testEntity = TestEntity.builder()
                    .name("hello")
                    .phoneNumber(3928492837598L)
                    .build();
            entityManagerImpl.persist(testEntity);

            FirstEntity first = new FirstEntity();
            entityManagerImpl.persist(first);

            List<FirstEntity> firstList = List.of(first);

            SuperEntity entity = SuperEntity.builder()
                    .first(firstList)
                    .build();

            entityManagerImpl.persist(entity);

            entity.setStringOne("hui");
            

            entityManagerImpl.persist(entity);



//            entityManager.executeUpdate("CREATE TABLE my_table(varchar)");
//            ResultSet rs0 = entityManager.executeQuery("CREATE TABLE my_table");
//            ResultSet rs1 = entityManager.executeQuery("SELECT * FROM my_table");

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Загрузите другие классы...
    }
}