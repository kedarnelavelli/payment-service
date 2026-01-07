openapi: 3.0.3
info:
  title: Payment Service API
  description: |
    API specification for the Payment Service.

    This service exposes payment operations such as purchase, authorize, capture,
    cancel (void), and refund. All payment APIs are secured using JWT authentication.
  version: 1.0.0

servers:
  - url: http://localhost:8080
    description: Local development server

security:
  - BearerAuth: []

paths:
  /api/auth/login:
    post:
      tags:
        - Authentication
      summary: Login and obtain JWT token
      description: Authenticates a user and returns a JWT token to be used for secured APIs.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                username:
                  type: string
                  example: assignment-user
                password:
                  type: string
                  example: password
              required:
                - username
                - password
      responses:
        '200':
          description: JWT token generated successfully
          content:
            text/plain:
              schema:
                type: string
                example: eyJhbGciOiJIUzI1NiJ9...
        '401':
          description: Invalid credentials

  /api/payments/purchase:
    post:
      tags:
        - Payments
      summary: Purchase (Authorize + Capture)
      description: Performs an immediate charge by authorizing and capturing funds.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PaymentRequest'
      responses:
        '200':
          description: Payment captured successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentOrderResponse'
        '400':
          description: Invalid payment request

  /api/payments/authorize:
    post:
      tags:
        - Payments
      summary: Authorize payment
      description: Authorizes funds without capturing them.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PaymentRequest'
      responses:
        '200':
          description: Payment authorized successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentOrderResponse'

  /api/payments/{orderId}/capture:
    post:
      tags:
        - Payments
      summary: Capture authorized payment
      description: Captures a previously authorized payment.
      security:
        - BearerAuth: []
      parameters:
        - name: orderId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Payment captured successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentOrderResponse'
        '400':
          description: Invalid payment state

  /api/payments/{orderId}/cancel:
    post:
      tags:
        - Payments
      summary: Cancel (Void) authorized payment
      description: Voids an authorized payment that has not yet been captured.
      security:
        - BearerAuth: []
      parameters:
        - name: orderId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Payment cancelled successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentOrderResponse'
        '400':
          description: Invalid payment state

  /api/payments/{orderId}/refund:
    post:
      tags:
        - Payments
      summary: Refund captured payment
      description: Performs a full or partial refund on a captured payment.
      security:
        - BearerAuth: []
      parameters:
        - name: orderId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                amount:
                  type: number
                  format: decimal
                  example: 100.00
              required:
                - amount
      responses:
        '200':
          description: Refund processed successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentOrderResponse'
        '400':
          description: Invalid payment state or refund amount

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    PaymentRequest:
      type: object
      properties:
        amount:
          type: number
          format: decimal
          example: 200.00
        currency:
          type: string
          example: USD
      required:
        - amount
        - currency

    PaymentOrderResponse:
      type: object
      properties:
        orderId:
          type: string
          format: uuid
        status:
          type: string
          example: CAPTURED
        amount:
          type: number
          format: decimal
        currency:
          type: string
      required:
        - orderId
        - status
        - amount
        - currency

