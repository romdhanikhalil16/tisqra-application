# Tisqra Platform - Principle Guide

## 1. Platform Overview
**Tisqra** (formerly known as event-ticketing-platform) is a comprehensive, microservices-based event ticketing and management platform. It facilitates the end-to-end flow of event creation, ticket purchasing, payment processing, ticket generation, and venue validation. 

The platform consists of a **Spring Boot Backend** composed of 8 distinct microservices communicating via REST and Kafka, and a **Flutter Mobile App** acting as the primary client.

---

## 2. Technical Stack
- **Backend Core**: Java 17+, Spring Boot, Spring Cloud (Eureka, API Gateway, Config Server).
- **Architecture**: Event-Driven Microservices.
- **Message Broker**: Apache Kafka (used for async service-to-service communication).
- **Databases**: PostgreSQL (Relational Data), Redis (Caching/Sessions).
- **Mobile Client**: Flutter 3.0+ (Clean Architecture).
- **Identity & Access Management**: Keycloak (local dev) / JWT-based custom auth.
- **Infrastructure & Deployment**: Docker, Docker Compose, Kubernetes.
- **CI/CD**: GitHub Actions, Jenkins.

---

## 3. Microservices Architecture
The backend is split into specialized domains. All client traffic flows through the **API Gateway** (`localhost:8080`).

| Service | Port | Primary Responsibilities |
|---------|------|--------------------------|
| **api-gateway** | `8080` | Edge proxy, request routing, and rate limiting. |
| **user-service** | `8081` | Authentication, user registration, role management, password resets. |
| **event-service** | `8083` | Event CRUD, ticket categories, pricing, and availability management. |
| **order-service** | `8084` | Order lifecycle (PENDING, CONFIRMED, CANCELLED), reserving and releasing tickets. |
| **ticket-service** | `8085` | Ticket generation (with QR codes), ticket transfer, and validation by scanners. |
| **payment-service** | `8086` | Payment processing (mock integration), publishes `payment.completed` or `payment.failed` to Kafka. |
| **notification-service** | `8087` | Consumes notification events to send Emails and Push Notifications. |
| **analytics-service** | `8088` | Consumes sales events to provide revenue and attendance analytics. |
| **organization-service**| `8089` | Organization CRUD, manages organizer subscriptions and creation limits. |

---

## 4. User Roles & Access
The platform utilizes Role-Based Access Control (RBAC) utilizing JWT tokens:
- **`GUEST`**: Regular end-users. Can browse events, purchase tickets, view their orders, and transfer tickets.
- **`SCANNER`**: Staff members at venues. Access is restricted to validating ticket QR codes via the mobile app.
- **`ADMIN_ORG`**: Organization administrators. Can create/edit events, manage ticket categories, and view analytics for their specific organization.
- **`SUPER_ADMIN`**: Global platform administrators. Can manage organizations, system-wide analytics, and platform configurations.

---

## 5. Core Business Flows (Event-Driven)

### 5.1. Ticket Purchase Flow
This is the most critical flow, highly dependent on Kafka for eventual consistency:
1. **User (GUEST)** browses events (`event-service`).
2. User creates an Order (`order-service`). Status is `PENDING`. Tickets are temporarily reserved.
3. User submits payment details to the `payment-service`.
4. `payment-service` processes the payment and publishes a `PaymentCompletedEvent` (or Failed) to the `payment.completed` Kafka topic.
5. `order-service` consumes this event:
   - If **Completed**: Updates order to `CONFIRMED`.
   - If **Failed**: Updates order to `CANCELLED` and releases the reserved tickets back to the `event-service`.
6. Once confirmed, a `TicketGeneratedEvent` is published, triggering the `ticket-service` to create actual QR-coded tickets.
7. `notification-service` detects the ticket generation and emails the user.

### 5.2. Ticket Validation Flow
1. A user with the **`SCANNER`** role opens the mobile app scanner.
2. They scan a `GUEST`'s QR code.
3. A request is sent to `POST /api/tickets/validate` on the `ticket-service`.
4. If valid, the ticket status changes to `VALIDATED` (preventing double entry).

---

## 6. Mobile Application (Flutter)
- Located in the `/mobile` directory.
- Built strictly using **Clean Architecture** (Presentation, Domain, Data layers).
- Contains logic for multiple user types within the same app:
  - **Public Routes**: Login, Register, Password Reset.
  - **App Shell (Guest)**: Home, Events, Orders, Tickets, Profile.
  - **Admin Shell**: Dashboards for `SUPER_ADMIN` and `ADMIN_ORG` to manage users and view analytics.
  - **Scanner Mode**: Blocked from regular app routes, isolated to ticket scanning capabilities.

---

## 7. Developer & Testing Workflow
- **Local Startup**: The platform can be booted entirely via PowerShell script (`.\dev-start.ps1 -FullStack`) or via `docker compose up -d`.
- **API Testing**: The repository contains a comprehensive Postman collection (`TISQRA_USER_FLOW_aligned.postman_collection.json`) mapping out the exact endpoint call order needed to test flows from end to end.
- **Shared Libraries**: Shared code (like Kafka event schemas) is kept in the `shared/` directory to prevent code duplication across microservices.

## AI Assistant Instructions
When assisting with this codebase:
1. Always account for **Event-Driven Architecture**. Do not assume synchronous REST calls for state changes across domains (e.g., Orders and Payments). Look for `@KafkaListener` and `KafkaTemplate` usages.
2. Ensure mobile app changes adhere to the existing **Clean Architecture** routing (`go_router`) and state management (`Riverpod` or equivalent, per project setup).
3. All APIs are prefixed with `/api/...` and routed through the `api-gateway`. Add auth headers (`Bearer Token`) for protected routes.
