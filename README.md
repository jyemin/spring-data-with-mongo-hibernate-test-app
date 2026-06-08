# spring-data-with-mongo-hibernate-test-app

A small Spring Boot 4.x application that uses MongoDB through JPA with the
[MongoDB Extension for Hibernate ORM](https://github.com/mongodb/mongo-hibernate) Spring Boot starter.
It exists to exercise and demonstrate the starter's design: Hibernate borrows the single Spring-managed
`MongoClient` rather than opening its own, so the application has one connection pool shared by JPA and
any direct driver use.

## What it demonstrates

- Activation through `spring.jpa.database-platform=MongoDB` (not `spring.datasource.url`).
- The connection configured with Spring Boot's standard `spring.mongodb.*` properties.
- Hibernate borrowing the Spring-managed `MongoClient` — one connection pool, visible through Actuator.
- Client customization through a `MongoClientSettingsBuilderCustomizer` bean (`MongoConfig`), the same
  hook any `spring-boot-mongodb` application uses. Here it sets `STANDARD` UUID representation (the entity's
  `@Id` is a `UUID`) and a pool size.
- Spring Data JPA repositories backed by MongoDB (`UserRepository`).

## Prerequisites

- JDK 17.
- A MongoDB replica set or sharded cluster on localhost:27017. 

## Running

```console
./gradlew bootRun
```

The application starts on port 8080.

## Endpoints

| Method | Path                  | Description                                                        |
|--------|-----------------------|--------------------------------------------------------------------|
| `POST` | `/user`               | Saves a `User` through the JPA repository.                         |
| `GET`  | `/users`              | Lists users through the JPA repository.                            |
| `GET`  | `/users/via-driver`   | Reads the same `User` collection through the raw `MongoClient` bean — the one Hibernate borrows. Returns the documents and the client's instance identity. |
| `GET`  | `/actuator/health`    | Health, including the `mongo` component.                           |
| `GET`  | `/actuator/metrics`   | Metric names; drill in with `/actuator/metrics/{name}`.            |

Example:

```console
curl -X POST localhost:8080/user
curl localhost:8080/users
curl localhost:8080/users/via-driver
```

## Verifying the single connection pool

The `mongodb.driver.pool.*` metrics report on the borrowed client's connection pool:

```console
curl localhost:8080/actuator/metrics/mongodb.driver.pool.size
```

The response carries a `cluster.id` tag with exactly one value: there is a single `MongoClient`, hence a
single pool. A separate Hibernate-owned client would appear as a second `cluster.id`. Actuator serves
JSON only; a browser JSON viewer renders it, or pipe through `jq`.

## How the borrow works

The starter brings the JPA stack (Spring Data JPA, Hibernate ORM, `spring-orm`) and `spring-boot-mongodb`,
but deliberately not a SQL connection pool — so Spring Boot's `DataSourceAutoConfiguration` stays inert and
the application needs no `spring.datasource.url`. When `spring.jpa.database-platform=MongoDB`, the starter
auto-configures the JPA `EntityManagerFactory`, a `JpaTransactionManager`, and repositories, and hands
Hibernate the `MongoClient` that `spring-boot-mongodb` builds from `spring.mongodb.uri`. This is not Spring
Data MongoDB.

`build.gradle` pins `hibernate.version = 7.3.7.Final`: the extension requires Hibernate ORM 7.3 or later,
and some Spring Boot 4.x releases manage an older Hibernate through their BOM.
