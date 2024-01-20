package ru.efimov.nsu.projects.objectmodel;

import ru.efimov.nsu.projects.objectmodel.persistence.annotations.Repository;
import ru.efimov.nsu.projects.objectmodel.persistence.interfaces.CrudRepository;
import ru.efimov.nsu.projects.objectmodel.test.TestEntity;

@Repository
public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}
