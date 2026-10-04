# object_persistence

**December 2023 – January 2024.** A Spring-style container and a JPA-style
persistence layer, rebuilt by hand in plain Java.

This was a study project written during a short hackathon at Novosibirsk State
University. The task was to build our own
analogue of Spring / Spring Boot with object persistence, working only from
memory of how the original framework behaves, and to implement the canonical
proxy pattern for repositories with Java dynamic proxies.

Its value is the same as the task: it is a low-level attempt to reproduce a
framework that is normally taken for granted. There is no Spring, no Hibernate
and no JPA API on the classpath — only reflection, JDBC and the PostgreSQL
driver.

## What is implemented

**Container** (`core/`, `BeanFactory`, `NoSpring`)

- `@Component` classpath scanning and `@Bean` factory methods
- `@Autowired` field injection, resolving dependencies recursively
- detection of cyclic dependencies while beans are being created
- `BeanPostProcessor` hooks before and after initialisation
- an application context that looks beans up by type or by name
- a `NoSpring.runApp(...)` entry point in the manner of `SpringApplication.run`

**Persistence** (`persistence/`)

- own annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`,
  `@JoinColumn`, `@OneToOne`, `@OneToMany`, `@ManyToOne`, with `mappedBy` and
  cascade types
- `EntityAnalyzer` reads entity classes through reflection into metadata
- `DefaultDatabaseInitializer` generates the schema from that metadata: tables,
  primary keys and foreign keys
- `EntityManagerImpl` does `persist` (insert or update, writing the generated
  key back into the entity), `remove` with cascading deletes, and relation
  handling
- connection settings come from `application.yml`

## The repository proxy, and where it stopped

`RepositoryProxy` is a `java.lang.reflect.InvocationHandler`. It takes a call to
a repository interface method such as `findByNameAndPhoneNumber`, splits the
method name into criteria, and turns it into
`SELECT * FROM … WHERE name = ? AND phone_number = ?` — the way Spring Data
derives queries from method names.

The handler is written, but the hackathon ended before it was wired in: the
`Proxy.newProxyInstance(...)` call in `RepositoryBeanPostProcessor` is still
commented out, so repository interfaces such as `TestEntityRepository` are not
yet backed by a proxy, and the `CrudRepository` methods have no implementation.
`find` in the entity manager and many-to-many relations are also unfinished.

## Running

Requires JDK 19 – 21 (the pinned Lombok version does not support newer JDKs),
Maven and Docker.

```sh
docker compose up -d      # PostgreSQL on localhost:6543
mvn compile exec:java -Dexec.mainClass=ru.efimov.nsu.projects.objectmodel.Application
```

`Application` starts the container, drops and recreates the schema for the
entities in the `test` package, and persists a few of them. It is a demo run,
not a test suite.

## Layout

```
src/main/java/ru/efimov/nsu/projects/objectmodel/
├── Application.java, NoSpring.java   entry point and bootstrap
├── BeanFactory.java                  bean creation and injection
├── core/                             context, annotations, post-processor API
├── persistence/annotations/          entity and relation annotations
├── persistence/interfaces/           Repository, CrudRepository
├── persistence/jpa/                  metadata, schema generation, entity manager
├── persistence/jpa/repository/       dynamic-proxy handler for repositories
└── test/                             sample entities used by the demo run
```
