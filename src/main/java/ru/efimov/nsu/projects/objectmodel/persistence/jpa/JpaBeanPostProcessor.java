//package ru.efimov.nsu.projects.objectmodel.persistence.jpa;
//
//import lombok.NoArgsConstructor;
//import ru.efimov.nsu.projects.objectmodel.core.Autowired;
//import ru.efimov.nsu.projects.objectmodel.core.BeanPostProcessor;
//import ru.efimov.nsu.projects.objectmodel.persistence.annotations.Entity;
//import ru.efimov.nsu.projects.objectmodel.persistence.annotations.ManyToMany;
//import ru.efimov.nsu.projects.objectmodel.persistence.annotations.OneToMany;
//import ru.efimov.nsu.projects.objectmodel.persistence.jpa.mapping.EntityMapping;
//import ru.efimov.nsu.projects.objectmodel.persistence.jpa.mapping.RelationMapping;
//
//import java.lang.reflect.Field;
//import java.util.Arrays;
//
//@NoArgsConstructor
//public class JpaBeanPostProcessor implements BeanPostProcessor {
//
//    private @Autowired JpaContext context;
//
//
//    @Override
//    public <T> T postProcessBeforeInitialization(T bean, String beanName) {
//        // Обработка до инициализации бина (если необходимо)
//        return bean;
//    }
//
//    @Override
//    public <T> T postProcessAfterInitialization(T bean, String beanName) {
//        // Обработка после инициализации бина
//        Class<?> beanClass = bean.getClass();
//
//        if (beanClass.isAnnotationPresent(Entity.class)) {
//            // Логика для обработки @Entity
//            processEntityAnnotation(bean, beanClass);
//        }
//
//        Arrays.stream(beanClass.getDeclaredFields())
//                .forEach(field -> {
//                    if (field.isAnnotationPresent(OneToMany.class)) {
//                        // Логика для обработки @OneToMany
//                        processOneToManyAnnotation(bean, field);
//                    }
//                    if (field.isAnnotationPresent(ManyToMany.class)) {
//                        // Логика для обработки @ManyToMany
//                        processManyToManyAnnotation(bean, field);
//                    }
//                });
//
//        return bean;
//    }
//
//    private void processEntityAnnotation(Object bean, Class<?> beanClass) {
//        EntityMapping mapping = new EntityMapping();
//        mapping.setEntityType(beanClass);
//        context.getEntityMappingsMap().put(beanClass, mapping);
//    }
//
//    private void processOneToManyAnnotation(Object bean, Field field) {
//        EntityMapping mapping = context.getEntityMappingsMap().get(bean.getClass());
//
//        var relationMapping = RelationMapping.builder()
//                .relationType(RelationMapping.RelationType.ONE_TO_MANY)
//                .fieldName(field.getName())
//                .targetEntity(field.getType())
//                .build();
//
//        mapping.getRelations().put(field.getName(), relationMapping);
//    }
//
//    private void processManyToManyAnnotation(Object bean, Field field) {
//        EntityMapping mapping = context.getEntityMappingsMap().get(bean.getClass());
//
//        var relationMapping = RelationMapping.builder()
//                .relationType(RelationMapping.RelationType.MANY_TO_MANY)
//                .fieldName(field.getName())
//                .targetEntity(field.getType())
//                .build();
//
//        mapping.getRelations().put(field.getName(), relationMapping);
//    }
//}