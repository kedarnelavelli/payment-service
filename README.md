# Payment Service

## Overview

The **Payment Service** is a backend application that implements a complete payment lifecycle using a clean, production-grade architecture. It supports common payment operations such as purchase, authorization, capture, cancellation, and refunds, while enforcing strict state transitions and business rules.

The service integrates with the **Authorize.Net Sandbox API** for payment processing and uses **JWT-based authentication** to secure all payment endpoints.

This project was built as part of a backend engineering assignment to demonstrate the ability to design, implement, test, and document a real-world backend system using modern Java and Spring Boot practices.

---

## Key Features

- Payment lifecycle management (Purchase, Authorize, Capture, Cancel, Refund)
- Strict state machine validation for payment actions
- Integration with Authorize.Net Sandbox (mocked for unit testing)
- JWT-based authentication and authorization
- Clean separation of concerns (Controller, Service, Gateway, Domain)
- Unit tests with high code coverage (JaCoCo)
- Environment-specific configuration using Spring Profiles
- Dockerized setup for easy validation

---

## Technology Stack

- **Java:** 21
- **Spring Boot:** 3.x
- **Spring Data JPA**
- **Spring Security (JWT)**
- **Hibernate**
- **H2 Database** (local development)
- **MySQL** (Docker environment)
- **Authorize.Net Java SDK**
- **JUnit 5 & Mockito**
- **JaCoCo** (code coverage)
- **Docker & Docker Compose**

---

## Configuration & Profiles

The application uses **Spring Profiles** to separate environment-specific configuration.

### Profiles

| Profile | Purpose | Database |
|-------|--------|---------|
| `local` | Local development | H2 (in-memory) |
| `docker` | Docker environment | MySQL |

### Configuration Files

- `application.yml` – Common configuration
- `application-local.yml` – Local (H2) configuration
- `application-docker.yml` – Docker (MySQL) configuration

---

## Running the Application

### Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose (for Docker setup)

---

### 1. Run Locally (H2 Database)

This is the default mode and requires no external database.

```bash
mvn spring-boot:run
```

- Application URL: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:paymentdb`
  - Username: `sa`
  - Password: (empty)

---

### 2. Run Using Docker Compose (MySQL)

This mode starts the application along with a MySQL database.

```bash
mvn clean package
docker-compose up --build
```

- Application URL: `http://localhost:8080`
- MySQL exposed on: `localhost:3308`

The Docker setup uses the `docker` Spring profile automatically.

---

## Authentication

The service uses **JWT-based authentication**.

### Login

```
POST /api/auth/login
```

Sample request body:
```json
{
  "username": "assignment-user",
  "password": "password"
}
```

The response contains a JWT token.

### Using the Token

All payment APIs require the following header:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## Payment APIs (Summary)

All payment APIs are secured using JWT authentication.

- `POST /api/payments/purchase`
- `POST /api/payments/authorize`
- `POST /api/payments/{orderId}/capture`
- `POST /api/payments/{orderId}/cancel`
- `POST /api/payments/{orderId}/refund`

Detailed request/response definitions are available in **API-SPECIFICATION.yml**.

---

## Background Workers

This application does not currently use background workers or schedulers. All payment operations are executed synchronously. The architecture allows for future extensions such as:

- Asynchronous payment reconciliation
- Scheduled settlement checks
- Outbox or event-driven processing

---

## Testing

- Unit tests are written using **JUnit 5** and **Mockito**
- Business logic and state transitions are fully covered
- External payment gateway interactions are mocked
- Code coverage is measured using **JaCoCo**

See:
- `TESTING_STRATEGY.md`
- `TEST_REPORT.md`

---

## Notes & Limitations

- Refund behavior in Authorize.Net Sandbox depends on transaction settlement and may not always be immediately testable
- Gateway integration is mocked for automated tests to ensure determinism

---

## Conclusion

This project demonstrates a realistic, production-grade backend service with clear separation of concerns, strong validation, comprehensive testing, and environment-aware configuration. It is designed to be easy to understand, run, and validate.

