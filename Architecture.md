# Payment Service – Architecture

## 1. Overview

This document describes the architecture of the **Payment Service**, including the overall design, major components, API endpoints, business flows, and database schema.

The service is a **backend-only payment orchestration system** that integrates with the **Authorize.Net Sandbox API** to support common payment operations such as purchase, authorization, capture, cancellation (void), and refund.

The primary goal of the system is to demonstrate **production-grade backend design**, with:
- Clear separation of concerns
- Explicit state machine enforcement
- External gateway isolation
- Strong unit test coverage of business logic

---

## 2. High-Level Architecture

The application follows a layered architecture:

```
Controller Layer (REST APIs)
        ↓
Service Layer (Business Logic, Transactions)
        ↓
Validation Layer (State Machine)
        ↓
Gateway Layer (Authorize.Net SDK)
        ↓
Persistence Layer (JPA / H2)
```

### Key Design Principles
- **Thin controllers**: Controllers only handle HTTP request/response mapping
- **Fat services**: All business rules live in the service layer
- **Explicit state validation**: All payment actions are validated against current state
- **Gateway abstraction**: External payment provider logic is isolated
- **Transactional consistency**: Each operation runs within a database transaction

---

## 3. Core Components

### 3.1 Controller Layer

Package:
```
com.talentica.payment.payment_service.controller
```

Responsibilities:
- Expose REST APIs
- Validate request payloads
- Delegate to service layer

Controllers do **not**:
- Contain business logic
- Interact with the database directly
- Call the Authorize.Net SDK

---

### 3.2 Service Layer

Package:
```
com.talentica.payment.payment_service.service
```

Main class:
- `PaymentService`

Responsibilities:
- Orchestrate payment workflows
- Enforce business rules
- Manage state transitions
- Persist orders and transactions
- Interact with payment gateway

Each public service method corresponds to a **business action**:
- Purchase
- Authorize
- Capture
- Cancel (Void)
- Refund

---

### 3.3 Validation Layer (State Machine)

Package:
```
com.talentica.payment.payment_service.validation
```

Main class:
- `PaymentStateValidator`

Responsibilities:
- Enforce valid state transitions
- Prevent invalid operations (e.g. refund before capture)

This ensures that **invalid payment flows are rejected early**, before calling the gateway.

---

### 3.4 Gateway Layer

Package:
```
com.talentica.payment.payment_service.gateway
```

Interface:
- `AuthorizeNetGateway`

Implementation:
- `AuthorizeNetGatewayImpl`

Responsibilities:
- Encapsulate Authorize.Net SDK calls
- Translate SDK responses into internal `GatewayResponse`
- Keep external API details out of service layer

The service layer depends only on the **gateway interface**, not the SDK itself.

---

### 3.5 Persistence Layer

Package:
```
com.talentica.payment.payment_service.repository
```

Uses:
- Spring Data JPA
- H2 in-memory database (for assignment/demo purposes)

Responsibilities:
- Persist payment orders
- Persist payment transactions

---

## 4. Payment Flows Implemented

### 4.1 Purchase Flow (AUTH + CAPTURE)

**Description:**
Performs an immediate charge on the customer.

**Flow:**
1. Create `PaymentOrder` in `CREATED` state
2. Validate PURCHASE action
3. Call Authorize.Net `AUTH_CAPTURE`
4. Persist transaction
5. Update order state to `CAPTURED` (or `FAILED`)

**Final State:** `CAPTURED`

---

### 4.2 Authorize Flow

**Description:**
Authorizes funds without capturing them.

**Flow:**
1. Create `PaymentOrder` in `CREATED` state
2. Validate AUTHORIZE action
3. Call Authorize.Net `AUTH_ONLY`
4. Persist transaction
5. Update order state to `AUTHORIZED` (or `FAILED`)

**Final State:** `AUTHORIZED`

---

### 4.3 Capture Flow

**Description:**
Captures a previously authorized payment.

**Flow:**
1. Fetch existing order
2. Validate CAPTURE action (must be `AUTHORIZED`)
3. Fetch last successful authorization transaction
4. Call Authorize.Net `PRIOR_AUTH_CAPTURE`
5. Persist transaction
6. Update order state to `CAPTURED` (or `FAILED`)

**Final State:** `CAPTURED`

---

### 4.4 Cancel (Void) Flow

**Description:**
Voids an authorized (but not captured) transaction.

**Flow:**
1. Fetch existing order
2. Validate CANCEL action (must be `AUTHORIZED`)
3. Fetch last successful transaction
4. Call Authorize.Net `VOID`
5. Persist transaction
6. Update order state to `CANCELLED` (or `FAILED`)

**Final State:** `CANCELLED`

---

### 4.5 Refund Flow

**Description:**
Refunds a captured transaction (partial or full).

**Flow:**
1. Fetch existing order
2. Validate REFUND action (must be `CAPTURED`)
3. Fetch last successful capture transaction
4. Call Authorize.Net `REFUND`
5. Persist transaction
6. Update order state to:
   - `REFUNDED` (full refund)
   - `PARTIALLY_REFUNDED` (partial refund)

**Final State:** `REFUNDED` or `PARTIALLY_REFUNDED`

⚠️ Note: In Authorize.Net sandbox, refunds are allowed only after settlement.

---

## 5. API Endpoints

Base path:
```
/api/payments
```

| Method | Endpoint | Description |
|------|---------|-------------|
| POST | `/purchase` | Purchase (authorize + capture) |
| POST | `/authorize` | Authorize payment |
| POST | `/{orderId}/capture` | Capture authorized payment |
| POST | `/{orderId}/cancel` | Cancel (void) authorized payment |
| POST | `/{orderId}/refund` | Refund captured payment |

---

## 6. Authentication & Security

The application uses **JWT-based authentication** to secure all payment APIs.

### Authentication Endpoint

```
POST /api/auth/login
```

**Description:**
Authenticates a user and returns a signed JWT token.

**Request Body (example):**
```json
{
  "username": "assignment-user",
  "password": "password"
}
```

**Response:**
```text
<JWT_TOKEN>
```

### Secured APIs

All `/api/payments/**` endpoints require:

```
Authorization: Bearer <JWT_TOKEN>
```

Requests without a valid token are rejected with `401 Unauthorized`.

---

---

## 6. Database Schema & Entity Relationships

### 6.1 PaymentOrder

Represents a logical payment request.

Fields:
- `id` (UUID, PK)
- `amount` (Decimal)
- `currency` (String)
- `status` (PaymentStatus)
- `createdAt`
- `updatedAt`

---

### 6.2 PaymentTransaction

Represents a gateway-level transaction attempt.

Fields:
- `id` (UUID, PK)
- `paymentOrder` (Many-to-One)
- `type` (PURCHASE, AUTHORIZE, CAPTURE, CANCEL, REFUND)
- `amount`
- `status` (SUCCESS / FAILED)
- `gatewayTransactionId`
- `createdAt`

---

### 6.3 Entity Relationship Diagram (Textual)

```
PaymentOrder 1 ──── * PaymentTransaction
```

- One order can have multiple transactions
- Transactions provide an audit trail of gateway interactions

---

## 7. State Machine Summary

| Current State | Allowed Actions |
|-------------|-----------------|
| CREATED | PURCHASE, AUTHORIZE |
| AUTHORIZED | CAPTURE, CANCEL |
| CAPTURED | REFUND |
| CANCELLED | — |
| REFUNDED | — |
| PARTIALLY_REFUNDED | REFUND |
| FAILED | — |

All state transitions are centrally enforced by `PaymentStateValidator`.

---

## 8. Summary

This payment service demonstrates:
- Clean layered architecture
- Explicit state machine design
- Safe integration with external payment gateway
- High unit test coverage for business logic
- Production-style error handling and validation

The design intentionally separates **business concerns** from **infrastructure concerns**, making the system easy to extend, test, and reason about.

