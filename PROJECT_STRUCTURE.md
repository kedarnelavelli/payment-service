# Project Structure – Payment Service

This document explains the structure of the **Payment Service** project and the responsibility of each package and key module. The structure follows standard Spring Boot conventions with clear separation of concerns to ensure maintainability, testability, and scalability.

---

## High-Level Directory Structure

```
payment-service
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.talentica.payment.payment_service
│   │   │       ├── config
│   │   │       ├── controller
│   │   │       ├── domain
│   │   │       │   ├── entity
│   │   │       │   └── enums
│   │   │       ├── dto
│   │   │       │   ├── request
│   │   │       │   └── response
│   │   │       ├── exception
│   │   │       ├── gateway
│   │   │       │   ├── dto
│   │   │       │   └── impl
│   │   │       ├── repository
│   │   │       ├── security
│   │   │       ├── service
│   │   │       └── validation
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── application-docker.yml
│   │
│   └── test
│       └── java
│           └── com.talentica.payment.payment_service
│               ├── service
│               └── validation
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── Architecture.md
├── API-SPECIFICATION.yml
├── TESTING_STRATEGY.md
├── TEST_REPORT.md
└── PROJECT_STRUCTURE.md
```

---

## Package-Level Explanation

### `config`
Contains configuration-related classes such as:
- Configuration properties (e.g. JWT properties)
- External service configuration bindings

Purpose:
- Centralize configuration logic
- Enable clean binding of `application.yml` properties

---

### `controller`
Contains REST controllers that expose HTTP APIs.

Responsibilities:
- Request validation
- Delegating requests to service layer
- Mapping requests and responses using DTOs

Controllers do **not** contain business logic.

---

### `domain`
Represents the core domain model of the application.

#### `domain.entity`
- JPA entities such as `PaymentOrder` and `PaymentTransaction`
- Defines database schema and relationships

#### `domain.enums`
- Domain enums like `PaymentStatus`, `PaymentAction`, `TransactionType`
- Used to enforce business rules and state transitions

---

### `dto`
Contains Data Transfer Objects used for API communication.

#### `dto.request`
- Request payload models for incoming API calls

#### `dto.response`
- Response payload models returned by APIs

DTOs isolate external API contracts from internal domain models.

---

### `exception`
Contains custom exception classes and global exception handling.

Responsibilities:
- Represent domain-specific error conditions
- Provide consistent error responses across APIs

---

### `gateway`
Represents the abstraction over external payment providers.

- Defines the `AuthorizeNetGateway` interface
- Allows the service layer to remain independent of specific gateway implementations

#### `gateway.impl`
- Concrete implementation using Authorize.Net Java SDK

#### `gateway.dto`
- Gateway-specific request/response mapping objects

---

### `repository`
Contains Spring Data JPA repositories.

Responsibilities:
- Database access
- Query abstraction

Repositories are injected into services and are not used directly by controllers.

---

### `security`
Implements JWT-based authentication and authorization.

Includes:
- JWT utility classes
- Authentication filter

Purpose:
- Secure all payment APIs
- Ensure stateless authentication

---

### `service`
Contains the core business logic of the application.

Key classes:
- `PaymentService`: Implements payment workflows (purchase, authorize, capture, cancel, refund)

Responsibilities:
- Orchestrate payment flows
- Interact with repositories and gateway
- Enforce transactional boundaries

---

### `validation`
Contains payment state validation logic.

Key class:
- `PaymentStateValidator`

Purpose:
- Enforce valid state transitions
- Centralize business rules related to payment lifecycle

---

## Resources

### Configuration Files

- `application.yml` – Common configuration
- `application-local.yml` – Local (H2) environment
- `application-docker.yml` – Docker (MySQL) environment

Spring Profiles are used to switch environments without code changes.

---

## Test Structure

```
src/test/java/com.talentica.payment.payment_service
├── service       # Unit tests for service layer
└── validation    # Unit tests for state validation logic
```

Testing focuses on:
- Business logic
- State transitions
- Failure scenarios

External systems and framework-driven components are mocked or excluded.

---

## Supporting Files

- `Dockerfile` – Builds the application container
- `docker-compose.yml` – Starts application and MySQL database
- `pom.xml` – Maven dependencies and build configuration

---

## Summary

The project structure follows standard Spring Boot best practices with a strong emphasis on separation of concerns. Each package has a clearly defined responsibility, enabling easy understanding, testing, and future extension of the system.

