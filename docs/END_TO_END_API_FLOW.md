## Tisqra Platform – End‑to‑End API Guide

This guide shows **how to run the stack**, and provides **end‑to‑end API flows** with example payloads from **signup / login** to **orders, payments, tickets, notifications**, with **roles** and **call order**.

It assumes:
- Backend running via `docker compose` (Keycloak, API gateway, services, DB, Kafka, Redis).
- You call APIs through the **API Gateway**: `http://localhost:8080`.

---

## 1. How to run the backend

From repo root:

```bash
docker compose up -d
```

Key endpoints:
- API Gateway: `http://localhost:8080`
- Keycloak: `http://localhost:8180`
- Eureka: `http://localhost:8761`

Wait until:

```bash
docker compose ps
```

shows all Java services `Up` (some may report `health: starting` briefly).

---

## 2. Roles & typical users

`UserRole` enum:
- **SUPER_ADMIN** – full platform admin.
- **ADMIN_ORG** – organization admin (manage event/organization order operations).
- **SCANNER** – validate tickets.
- **GUEST** – regular end‑user buying tickets.

In flows below we mainly use **GUEST** (customer) and mention where **ADMIN_ORG / SUPER_ADMIN / SCANNER** is required.

---

## 3. Authentication & User Management

### 3.1 Register (Signup) – GUEST

**POST** `/api/auth/register`

Request body:

```json
{
  "email": "john.doe@example.com",
  "password": "Str0ngP@ssword!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+21612345678",
  "role": "GUEST"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": "c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+21612345678",
    "role": "GUEST",
    "isActive": true,
    "emailVerified": false
  }
}
```

> Notification service will (eventually) send an email verification link/token via Kafka.

### 3.2 Verify Email (token‑based)

**POST** `/api/auth/email/verify?token=...`

Example:

```http
POST /api/auth/email/verify?token=VERIFICATION_TOKEN
Authorization: (none)
```

Response:

```json
{ "success": true, "data": null }
```

### 3.3 Login (email/password)

**POST** `/api/auth/login`

Request:

```json
{
  "email": "john.doe@example.com",
  "password": "Str0ngP@ssword!"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9....",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9....",
    "tokenType": "Bearer",
    "expiresIn": 300,
    "user": {
      "id": "c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "GUEST",
      "isActive": true,
      "emailVerified": true
    }
  }
}
```

> Use `Authorization: Bearer <accessToken>` for all protected endpoints.

### 3.4 Forgot / Reset Password

**Request reset link**  
`POST /api/auth/password/reset-request?email=john.doe@example.com`

```json
{ "success": true, "data": null }
```

**Reset with token**  
`POST /api/auth/password/reset`

```json
{
  "token": "RESET_TOKEN_FROM_EMAIL",
  "newPassword": "NewStr0ngP@ss"
}
```

---

## 4. Customer Flow – Browse Events → Order → Pay → Tickets

### 4.1 List Events (public / authenticated)

Depending on your event-service controllers (typical pattern):

**GET** `/api/events?status=ACTIVE&page=0&size=10`

Response (example):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "5a1d91df-2fa1-4eb1-9113-df9c7dbeb123",
        "name": "Tech Conference 2026",
        "category": "CONFERENCE",
        "status": "PUBLISHED",
        "startDate": "2026-06-20T09:00:00",
        "endDate": "2026-06-20T17:00:00",
        "location": "Tunis",
        "organizerId": "27b76b0f-5689-43a1-9c7a-d115927aabcd"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 4.2 Get Ticket Categories for an Event

**GET** `/api/events/{eventId}/ticket-categories`

Example:

```http
GET /api/events/5a1d91df-2fa1-4eb1-9113-df9c7dbeb123/ticket-categories
Authorization: Bearer <accessToken>
```

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "0b7c6442-4d87-4f40-bd3b-4fbb95a16c10",
      "name": "Standard",
      "price": 50.0,
      "currency": "EUR",
      "availableQuantity": 200
    },
    {
      "id": "f4bdf971-90e3-4fcb-8b7d-e5da009ac456",
      "name": "VIP",
      "price": 120.0,
      "currency": "EUR",
      "availableQuantity": 50
    }
  ]
}
```

### 4.3 Create Order (reserve tickets)

**POST** `/api/orders` (role: any authenticated user)

Request:

```json
{
  "eventId": "5a1d91df-2fa1-4eb1-9113-df9c7dbeb123",
  "items": [
    {
      "ticketCategoryId": "0b7c6442-4d87-4f40-bd3b-4fbb95a16c10",
      "quantity": 2
    },
    {
      "ticketCategoryId": "f4bdf971-90e3-4fcb-8b7d-e5da009ac456",
      "quantity": 1
    }
  ],
  "promoCode": "WELCOME10"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": "1c3d77a2-6e9a-4dd4-8cdd-2dd902ce9b42",
    "orderNumber": "ORD-20260330-0001",
    "userId": "c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1",
    "eventId": "5a1d91df-2fa1-4eb1-9113-df9c7dbeb123",
    "status": "PENDING",
    "subtotal": 220.0,
    "discountAmount": 22.0,
    "totalAmount": 198.0,
    "currency": "EUR",
    "items": [
      {
        "ticketCategoryId": "0b7c6442-4d87-4f40-bd3b-4fbb95a16c10",
        "ticketCategoryName": "Standard",
        "quantity": 2,
        "unitPrice": 50.0,
        "totalPrice": 100.0
      },
      {
        "ticketCategoryId": "f4bdf971-90e3-4fcb-8b7d-e5da009ac456",
        "ticketCategoryName": "VIP",
        "quantity": 1,
        "unitPrice": 120.0,
        "totalPrice": 120.0
      }
    ]
  }
}
```

### 4.4 Process Payment

**POST** `/api/payments/process`

Request:

```json
{
  "orderId": "1c3d77a2-6e9a-4dd4-8cdd-2dd902ce9b42",
  "paymentMethod": "CARD",
  "cardNumber": "4242424242424242",
  "cardHolderName": "John Doe",
  "expiryMonth": "06",
  "expiryYear": "2028",
  "cvv": "123"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": "c5af0ae6-e4d4-42ef-a5e4-68b96978c2f7",
    "orderId": "1c3d77a2-6e9a-4dd4-8cdd-2dd902ce9b42",
    "amount": 198.0,
    "currency": "EUR",
    "method": "CARD",
    "provider": "MOCK",
    "providerPaymentId": "PAY-123456",
    "status": "COMPLETED",
    "paidAt": "2026-03-30T13:40:10",
    "createdAt": "2026-03-30T13:40:10"
  }
}
```

> Payment service publishes a **PaymentCompletedEvent** to Kafka; order-service listens and moves order to `CONFIRMED`/`PROCESSING`.

### 4.5 Generate Tickets

In this architecture tickets are usually generated **automatically** from order/payment events, but there is also an explicit API:

**POST** `/api/tickets/generate`

Request:

```json
{
  "orderId": "1c3d77a2-6e9a-4dd4-8cdd-2dd902ce9b42"
}
```

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "e23eecfa-4f7c-44c2-9c5d-2f04a91d2701",
      "ticketNumber": "TCK-0001-ABCDEF",
      "orderId": "1c3d77a2-6e9a-4dd4-8cdd-2dd902ce9b42",
      "eventId": "5a1d91df-2fa1-4eb1-9113-df9c7dbeb123",
      "ticketCategoryId": "0b7c6442-4d87-4f40-bd3b-4fbb95a16c10",
      "ownerEmail": "john.doe@example.com",
      "ownerName": "John Doe",
      "status": "ACTIVE",
      "qrCode": "QR-PAYLOAD-BASE64-OR-TEXT"
    }
  ]
}
```

> Ticket service publishes **TicketGeneratedEvent**; notification-service can send an email with the ticket.

### 4.6 List My Orders

**GET** `/api/orders/user/{userId}?page=0&size=10`

Example:

```http
GET /api/orders/user/c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1?page=0&size=10
Authorization: Bearer <accessToken>
```

### 4.7 List My Tickets

**GET** `/api/tickets/user/{userId}?page=0&size=20`

Example:

```http
GET /api/tickets/user/c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1?page=0&size=20
Authorization: Bearer <accessToken>
```

Response (simplified):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "e23eecfa-4f7c-44c2-9c5d-2f04a91d2701",
        "ticketNumber": "TCK-0001-ABCDEF",
        "status": "ACTIVE",
        "eventId": "5a1d91df-2fa1-4eb1-9113-df9c7dbeb123",
        "ownerEmail": "john.doe@example.com"
      }
    ]
  }
}
```

---

## 5. Ticket Validation & Transfer (SCANNER / GUEST)

### 5.1 Validate Ticket (SCANNER role)

Scanner app / user scans QR and calls:

**POST** `/api/tickets/validate?qrCode=...&scannerId=...&scannerName=...`

Example:

```http
POST /api/tickets/validate?qrCode=TCK-0001-ABCDEF&scannerId=5a50...&scannerName=Gate%201
Authorization: Bearer <scannerAccessToken>
```

Response:

```json
{
  "success": true,
  "data": {
    "ticketNumber": "TCK-0001-ABCDEF",
    "status": "VALIDATED",
    "validatedAt": "2026-06-20T08:55:00",
    "validatedBy": "scanner-user-id",
    "scannerDeviceId": "5a50..."
  }
}
```

### 5.2 Transfer Ticket (owner → new email) – GUEST

**POST** `/api/tickets/{ticketId}/transfer`

Request:

```json
{
  "newOwnerEmail": "friend@example.com",
  "newOwnerName": "Friend Name"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "ticketNumber": "TCK-0001-ABCDEF",
    "status": "TRANSFERRED",
    "ownerEmail": "friend@example.com",
    "ownerName": "Friend Name"
  }
}
```

---

## 6. Notifications (IN_APP / EMAIL / PUSH)

### 6.1 List My Notifications

**GET** `/api/notifications/user/{userId}?page=0&size=20`

```http
GET /api/notifications/user/c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1?page=0&size=20
Authorization: Bearer <accessToken>
```

Example response:

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "98c5c0d4-45b7-4f09-a753-d2f17dcf9a31",
        "userId": "c0fbcf05-d0b0-4f1f-bb85-4d634c3e0ba1",
        "type": "TICKET_PURCHASE",
        "channel": "EMAIL",
        "subject": "Your tickets for Tech Conference 2026",
        "content": "Hi John, your tickets are attached...",
        "sent": true,
        "read": false,
        "createdAt": "2026-03-30T13:41:00"
      }
    ]
  }
}
```

### 6.2 Mark Notification as Read

**POST** `/api/notifications/{notificationId}/read`

```http
POST /api/notifications/98c5c0d4-45b7-4f09-a753-d2f17dcf9a31/read
Authorization: Bearer <accessToken>
```

Response:

```json
{ "success": true, "data": null }
```

### 6.3 Delete Notification

**DELETE** `/api/notifications/{notificationId}`

```http
DELETE /api/notifications/98c5c0d4-45b7-4f09-a753-d2f17dcf9a31
Authorization: Bearer <accessToken>
```

---

## 7. Admin / Organization Flows (HIGH LEVEL)

These are typical for **SUPER_ADMIN** / **ADMIN_ORG** and match your controller annotations:

- Manage organizations (`organization-service`):
  - Create org: `POST /api/organizations` (**SUPER_ADMIN**)
  - Check subscription can create event: `GET /api/organizations/{id}/can-create-event`
  - Increment event count: `POST /api/organizations/{id}/increment-event-count`

- Manage events (`event-service`, **ADMIN_ORG**):
  - Create/edit events: e.g. `POST /api/events`, `PUT /api/events/{id}`
  - Manage ticket categories: `POST/PUT /api/events/{id}/ticket-categories`

- Analytics (`analytics-service`, **SUPER_ADMIN / ADMIN_ORG**):
  - E.g. event sales, revenue endpoints under `/api/analytics/...`

Because these are mostly administrative and specific to your domain, use Swagger UIs of each service to inspect the exact DTOs (all responses are wrapped in `ApiResponse<T>`).

---

## 8. Recommended Call Order (Happy Path)

**As GUEST (end-user):**

1. `POST /api/auth/register`
2. `POST /api/auth/email/verify?token=...` (if you use email verification)
3. `POST /api/auth/login` → capture `accessToken`, `user.id`
4. `GET /api/events?...` (browse events)
5. `GET /api/events/{eventId}/ticket-categories`
6. `POST /api/orders` (create order)
7. `POST /api/payments/process` (complete payment)
8. (optional) `POST /api/tickets/generate`
9. `GET /api/orders/user/{userId}` (see order history)
10. `GET /api/tickets/user/{userId}` (see tickets)
11. At venue: `POST /api/tickets/validate?qrCode=...` (SCANNER)
12. Optional transfer: `POST /api/tickets/{ticketId}/transfer`
13. View notifications: `GET /api/notifications/user/{userId}`

You can exercise these with Postman or the Flutter mobile app (which already follows this flow end‑to‑end).

