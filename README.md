# 🎫 Tisqra Platform

A production-ready SaaS microservices platform for event management and ticketing.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Services](#services)
- [API Documentation](#api-documentation)
- [Development](#development)

## 🎯 Overview

A comprehensive event management and ticketing platform built with microservices architecture, featuring:

- **Multi-tenant SaaS** with subscription plans
- **Event management** with ticket categories and schedules
- **QR code ticketing** with mobile validation
- **Payment processing** with refund support
- **Real-time analytics** and dashboards
- **Email and push notifications**
- **Role-based access control** (RBAC)

## 🏗️ Architecture

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                      │
│              JWT Authentication & Rate Limiting              │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌────────▼────────┐
│   Identity     │  │ Organization │  │     Event       │
│   Service      │  │   Service    │  │   Service       │
│   (8081)       │  │   (8082)     │  │   (8083)        │
└────────────────┘  └──────────────┘  └─────────────────┘
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌────────▼────────┐
│     Order      │  │    Ticket    │  │    Payment      │
│   Service      │  │   Service    │  │   Service       │
│   (8084)       │  │   (8085)     │  │   (8086)        │
└────────────────┘  └──────────────┘  └─────────────────┘
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐           │
│ Notification   │  │  Analytics   │           │
│   Service      │  │   Service    │           │
│   (8087)       │  │   (8088)     │           │
└────────────────┘  └──────────────┘           │
                                                │
        ┌───────────────────────────────────────┘
        │
┌───────▼────────────────────────────────────────────┐
│              Apache Kafka Event Bus                │
│  (Event-Driven Communication & Async Processing)   │
└────────────────────────────────────────────────────┘
```

### Infrastructure Services

- **Eureka Discovery Service** (8761) - Service registry
- **Config Server** (8888) - Centralized configuration
- **API Gateway** (8080) - Edge service with security
- **Keycloak** (8180) - Identity and access management
- **PostgreSQL** (5432) - Database per service
- **Redis** (6379) - Caching layer
- **Kafka + Zookeeper** (9092, 2181) - Event streaming

## 🛠️ Tech Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.2.2** - Application framework
- **Spring Cloud 2023.0.0** - Microservices patterns
- **Apache Kafka** - Event streaming
- **PostgreSQL 16** - Relational database
- **Redis 7** - In-memory cache
- **Keycloak 23** - Authentication & authorization
- **Flyway** - Database migrations
- **Docker & Docker Compose** - Containerization

### Mobile
- **Clean Architecture** - Separation of concerns
- **Firebase Cloud Messaging** - Push notifications

### Additional Libraries
- **MapStruct** - Object mapping
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation
- **ZXing** - QR code generation

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **Git**

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd tisqra-platform
```

2. **Configure environment variables**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start infrastructure services**
```bash
docker-compose up -d postgres redis kafka zookeeper keycloak
```

4. **Wait for services to be healthy**
```bash
docker-compose ps
```

5. **Build all services**
```bash
mvn clean install
```

6. **Start microservices**
```bash
# Option 1: Using Docker Compose (recommended)
docker-compose up --build

# Option 2: Run individually
cd infrastructure/discovery-service && mvn spring-boot:run
cd infrastructure/config-server && mvn spring-boot:run
cd infrastructure/api-gateway && mvn spring-boot:run
# ... repeat for each service
```

7. **Access the services**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Keycloak Admin: http://localhost:8180 (admin/admin)
- API Documentation: http://localhost:8080/swagger-ui.html

## 📦 Services

### Business Microservices

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| **user-service** | 8081 | user_db | User management and authentication |
| **organization-service** | 8082 | organization_db | Multi-tenant organization & subscriptions |
| **event-service** | 8083 | event_db | Event creation and management |
| **order-service** | 8084 | order_db | Order processing and cart management |
| **ticket-service** | 8085 | ticket_db | Ticket generation and QR validation |
| **payment-service** | 8086 | payment_db | Payment processing and refunds |
| **notification-service** | 8087 | notification_db | Email and push notifications |
| **analytics-service** | 8088 | analytics_db | Sales analytics and dashboards |

### Kafka Topics

```
# Organization Events
organization.created
subscription.upgraded

# Event Events
event.published
ticket.category.created

# Order Events
order.created
payment.completed
payment.failed

# Ticket Events
ticket.generated
ticket.validated
ticket.transferred

# Notification Events
notification.email.send
notification.push.send

# Analytics Events
analytics.sales.recorded
```


## 📚 API Documentation

Each service exposes OpenAPI documentation:

- **API Gateway Aggregated Docs**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Organization Service**: http://localhost:8082/swagger-ui.html
- **Event Service**: http://localhost:8083/swagger-ui.html
- **Order Service**: http://localhost:8084/swagger-ui.html
- **Ticket Service**: http://localhost:8085/swagger-ui.html
- **Payment Service**: http://localhost:8086/swagger-ui.html
- **Notification Service**: http://localhost:8087/swagger-ui.html
- **Analytics Service**: http://localhost:8088/swagger-ui.html

## 🔐 Security & Roles

### Keycloak Roles

- **SUPER_ADMIN** - Platform administrator
- **ADMIN_ORG** - Organization administrator
- **SCANNER** - Event entrance scanner
- **GUEST** - Regular user/attendee

### Authentication Flow

1. User authenticates via Keycloak
2. Receives JWT token
3. API Gateway validates token
4. Routes request to microservice
5. Service validates role-based permissions

## 🧪 Development

### Project Structure

```
tisqra-platform/
├── infrastructure/          # Infrastructure services
│   ├── discovery-service/  # Eureka
│   ├── config-server/      # Spring Cloud Config
│   └── api-gateway/        # API Gateway
├── services/               # Business microservices
│   ├── user-service/
│   ├── organization-service/
│   ├── event-service/
│   ├── order-service/
│   ├── ticket-service/
│   ├── payment-service/
│   ├── notification-service/
│   └── analytics-service/
├── shared/                 # Shared libraries
│   ├── common-models/      # Common DTOs and utilities
│   └── kafka-events/       # Kafka event schemas
├── docker/                 # Docker configurations
├── docs/                   # Documentation
├── scripts/                # Utility scripts
└── postman/               # API collections
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd services/event-service
mvn test
```

### Database Migrations

Flyway migrations are located in each service under:
```
src/main/resources/db/migration/
```

## 🤝 Contributing

This is an academic and portfolio project. Contributions are welcome!

## 📄 License

This project is for educational purposes.

## 👨‍💻 Author

Built as a portfolio project demonstrating enterprise-level microservices architecture.
