# Testing Strategy – Payment Service

## 1. Objective

The objective of this testing strategy is to ensure that the **Payment Service** behaves correctly, safely, and predictably under various scenarios, while maintaining high confidence in business-critical payment logic.

The strategy focuses on:
- Validating core business rules
- Ensuring correctness of payment state transitions
- Safely integrating with an external payment gateway
- Achieving high, meaningful test coverage

---

## 2. Testing Philosophy

The testing approach follows these principles:

- **Business-logic first**: Critical payment logic must be tested thoroughly
- **Fast feedback**: Prefer unit tests over slow integration tests
- **Isolation**: External systems are mocked
- **Determinism**: Tests must be repeatable and environment-independent
- **Meaningful coverage**: Focus on logic, not boilerplate

---

## 3. Test Levels

### 3.1 Unit Testing (Primary Focus)

Unit tests form the core of the testing strategy.

**What is tested:**
- Payment workflows in `PaymentService`
- Payment state transitions in `PaymentStateValidator`
- Error handling and failure paths

**What is NOT tested:**
- HTTP layer behavior
- Database integration
- External payment gateway SDK

**Tools Used:**
- JUnit 5
- Mockito

---

### 3.2 Integration Testing (Limited Scope)

Integration tests are intentionally limited due to:
- External gateway dependency
- Sandbox behavior limitations

**Covered areas:**
- Repository wiring with H2 database
- Application context startup

**Not covered:**
- Real Authorize.Net API calls

---

### 3.3 Manual Testing (Postman)

Manual testing is performed using Postman to validate:
- End-to-end API flows
- Authentication using JWT
- Request/response correctness

This complements automated testing for realistic user scenarios.

---

## 4. Test Scope by Layer

### 4.1 Service Layer

**Reason:**
This layer contains the most critical business logic.

**Scenarios tested:**
- Successful purchase, authorize, capture, cancel, and refund
- Gateway failures
- Invalid state transitions
- Partial vs full refund behavior

**Outcome:**
High confidence in payment orchestration logic.

---

### 4.2 Validation Layer (State Machine)

**Reason:**
Ensures invalid payment flows are blocked early.

**Scenarios tested:**
- Allowed state transitions
- Disallowed state transitions
- Correct exception types and messages

**Outcome:**
Guarantees correctness of payment lifecycle rules.

---

### 4.3 Gateway Layer

**Approach:**
- Mocked in unit tests
- Not directly unit-tested

**Reason:**
- Contains external SDK calls
- Behavior depends on Authorize.Net sandbox

**Verification:**
- Correct invocation verified via mocks

---

### 4.4 Controller Layer

**Approach:**
- Thin layer
- Minimal logic

**Testing:**
- Verified via manual Postman testing
- Not covered by unit tests

---

## 5. External Dependency Handling

### Authorize.Net Sandbox

- Gateway calls are mocked in unit tests
- Sandbox behavior is unreliable for settlement-based operations

**Known limitation:**
- Refunds require transaction settlement
- Sandbox transactions may remain in `CAPTURED` state indefinitely

This limitation is handled by:
- Testing refund logic using mocks
- Documenting behavior clearly

---

## 6. Test Data Strategy

- Static, deterministic test data
- UUIDs generated in tests
- Monetary values kept simple (e.g., 100, 200)

This avoids flaky tests and improves readability.

---

## 7. Code Coverage Strategy

### Tool Used
- JaCoCo

### Coverage Focus
- Service layer
- Validation layer

### Explicit Exclusions
- Controllers
- DTOs
- Entities
- Repositories
- Configuration and security
- Gateway implementations

**Rationale:**
These components contain minimal or framework-driven logic.

### Achieved Coverage
- Instruction coverage: ~96%
- Branch coverage: ~78%

---

## 8. Failure & Edge Case Testing

The following scenarios are explicitly tested:
- Gateway failures
- Invalid payment state transitions
- Partial refunds
- Missing transactions for capture/refund

These tests ensure robustness under real-world failure conditions.

---

## 9. Summary

This testing strategy ensures:
- Strong confidence in payment business logic
- Fast and reliable test execution
- Meaningful coverage metrics
- Clear handling of external system limitations

The approach balances **correctness**, **maintainability**, and **practicality**, aligning with production-grade backend development standards.

