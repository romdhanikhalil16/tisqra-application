# 🚀 Tisqra Platform - Backend Startup Guide

## ✅ Current Status
**All 16 services are running and ready!**

### Running Services (16/16)
- ✅ PostgreSQL (Database)
- ✅ Redis (Cache)
- ✅ Kafka & Zookeeper (Message Queue)
- ✅ Keycloak (Identity & Access Management)
- ✅ Discovery Service (Eureka)
- ✅ Config Server
- ✅ API Gateway
- ✅ User Service
- ✅ Organization Service
- ✅ Event Service
- ✅ Order Service
- ✅ Ticket Service
- ✅ Payment Service
- ✅ Notification Service
- ✅ Analytics Service

---

## 📊 Database Credentials

```
Host: localhost
Port: 5432
Username: postgres
Password: root
Database: eventticket_db
```

### Accessing PostgreSQL
```bash
# Via Docker
docker exec -it eventticket-postgres psql -U postgres -d eventticket_db

# Via psql CLI (if installed locally)
psql -h localhost -U postgres -d eventticket_db
```

---

## 🔗 API Endpoints

### Infrastructure Services
| Service | URL | Purpose |
|---------|-----|---------|
| **API Gateway** | http://localhost:8080 | Main entry point for all API requests |
| **Discovery Service** | http://localhost:8761 | Eureka service registry |
| **Config Server** | http://localhost:8888 | Centralized configuration management |
| **Keycloak** | http://localhost:8180 | Identity & Access Management |

### Business Microservices
| Service | Port | Base URL |
|---------|------|----------|
| User Service | 8081 | http://localhost:8081 |
| Organization Service | 8082 | http://localhost:8082 |
| Event Service | 8083 | http://localhost:8083 |
| Order Service | 8084 | http://localhost:8084 |
| Ticket Service | 8085 | http://localhost:8085 |
| Payment Service | 8086 | http://localhost:8086 |
| Notification Service | 8087 | http://localhost:8087 |
| Analytics Service | 8088 | http://localhost:8088 |

### Infrastructure
| Component | Host | Port | Credentials |
|-----------|------|------|-------------|
| PostgreSQL | localhost | 5432 | postgres/root |
| Redis | localhost | 6379 | Password: root |
| Kafka | localhost | 9092 | - |
| Zookeeper | localhost | 2181 | - |

---

## 🏗️ Architecture Overview

### Microservices Pattern
- **Service Discovery**: Eureka (Spring Cloud Discovery)
- **Configuration**: Spring Cloud Config Server
- **API Gateway**: Spring Cloud Gateway
- **Message Queue**: Apache Kafka
- **Cache**: Redis
- **Database**: PostgreSQL
- **Identity**: Keycloak (OAuth2/OIDC)

### Technology Stack
- **Framework**: Spring Boot 3.2.2
- **Java Version**: 17
- **Container Orchestration**: Docker Compose
- **Event Streaming**: Apache Kafka 3.6.1
- **Database**: PostgreSQL 15
- **Cache**: Redis 7.2
- **Build Tool**: Maven 3.8+

---

## 📁 Project Structure

```
tisqra-platform/
├── infrastructure/
│   ├── discovery-service/      # Eureka service registry
│   ├── config-server/          # Spring Cloud Config
│   └── api-gateway/            # Spring Cloud Gateway
│
├── services/                   # 8 Business Microservices
│   ├── user-service/           # User management
│   ├── organization-service/   # Organization & subscription
│   ├── event-service/          # Event management
│   ├── order-service/          # Order processing
│   ├── ticket-service/         # Ticket operations
│   ├── payment-service/        # Payment processing
│   ├── notification-service/   # Email/SMS notifications
│   └── analytics-service/      # Event analytics
│
├── shared/                     # Shared libraries
│   ├── common-models/          # DTOs & Common classes
│   └── kafka-events/           # Kafka event definitions
│
├── mobile/                     # Flutter mobile app
│   └── tisqra_mobile_app/
│
└── deployment/                 # K8s & production configs
    ├── docker/
    ├── kubernetes/
    └── scripts/
```

---

## 🔄 Service Communication Flow

```
Client Request
    ↓
API Gateway (8080)
    ↓
Service Discovery (Eureka - 8761)
    ↓
Target Microservice
    ↓
PostgreSQL (8 databases) + Redis Cache
    ↓
Kafka Events (for async communication)
```

---

## 🛠️ Common Commands

### View All Logs
```bash
docker-compose logs -f
```

### View Specific Service Logs
```bash
docker-compose logs -f user-service
docker-compose logs -f event-service
docker-compose logs -f api-gateway
```

### Check Service Status
```bash
docker-compose ps
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart user-service
```

### Stop Services
```bash
docker-compose down
```

### Stop Services & Remove Volumes
```bash
docker-compose down -v
```

---

## 📝 Database Setup

### Auto-Migration
The services use **Flyway** for database migrations. Each service automatically:
1. Creates required databases on startup
2. Runs migration scripts from `src/main/resources/db/migration/`
3. Initializes schema and default data

### Database List
- `eventticket_user` - User Service
- `eventticket_organization` - Organization Service
- `eventticket_event` - Event Service
- `eventticket_order` - Order Service
- `eventticket_ticket` - Ticket Service
- `eventticket_payment` - Payment Service
- `eventticket_notification` - Notification Service
- `eventticket_analytics` - Analytics Service

---

## 🔐 Security

### Keycloak Setup
- **URL**: http://localhost:8180
- **Admin Console**: http://localhost:8180/admin
- **Realm**: tisqra-realm (pre-configured)
- **Default Setup**: Configured via `realm-export.json`

### OAuth2/OIDC
All services are secured with OAuth2. The API Gateway validates tokens before routing to microservices.

---

## 📦 Kafka Topics

Kafka is used for event-driven communication between services. Topics are auto-created:

| Topic | Purpose | Producer | Consumers |
|-------|---------|----------|-----------|
| `user-events` | User creation/updates | User Service | Notification, Analytics |
| `event-events` | Event operations | Event Service | Notification, Analytics |
| `order-events` | Order processing | Order Service | Payment, Notification |
| `ticket-events` | Ticket operations | Ticket Service | Notification, Analytics |
| `payment-events` | Payment status | Payment Service | Notification, Order |

---

## 🧪 Testing Services

### Health Check Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Swagger/OpenAPI Documentation
Each service has built-in Swagger UI:
```
http://localhost:{PORT}/swagger-ui.html
```

Example:
- User Service: http://localhost:8081/swagger-ui.html
- Event Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html

---

## 📊 Monitoring & Logs

### Docker Compose Logs
```bash
# View logs for all services
docker-compose logs -f

# View logs for specific service with timestamp
docker-compose logs -f --timestamps user-service

# View last 100 lines
docker-compose logs --tail=100 api-gateway
```

### Application Logs
Logs are configured via `application.yml` in each service:
- Log level: INFO (can be adjusted)
- Format: JSON or text
- Destination: Console (Docker)

---

## 🚀 Next Steps

1. **Test API Endpoints**
   - Use Postman or curl to test endpoints
   - Postman collections available in `/postman` directory

2. **Mobile App Development**
   - Navigate to `mobile/tisqra_mobile_app/`
   - Run Flutter app with `flutter run`

3. **Explore Microservices**
   - Check service implementations in `services/` directory
   - Review Swagger UI at each service's `/swagger-ui.html` endpoint

4. **Monitor Services**
   - Check Eureka dashboard at http://localhost:8761
   - View logs with `docker-compose logs -f`

5. **Database Management**
   - Connect with postgres client
   - Query via psql or GUI tools (DBeaver, PgAdmin)

---

## ⚙️ Configuration Files

### Environment Variables
- `.env` - Main environment configuration

### Application Configuration
- `infrastructure/discovery-service/src/main/resources/application.yml`
- `infrastructure/config-server/src/main/resources/application.yml`
- `infrastructure/api-gateway/src/main/resources/application.yml`
- Each service has its own `application.yml` and `application-docker.yml`

---

## 🐛 Troubleshooting

### Services not starting?
```bash
# Check logs
docker-compose logs {service-name}

# Restart service
docker-compose restart {service-name}

# View resource usage
docker stats
```

### Database connection issues?
```bash
# Check PostgreSQL container
docker logs eventticket-postgres

# Verify credentials
docker exec -it eventticket-postgres psql -U postgres -d eventticket_db
```

### Port conflicts?
If ports are already in use, modify `docker-compose.yml` port mappings and restart services.

---

## 📚 Documentation Files

- `README.md` - Main project overview
- `COMPLETE_PROJECT_SUMMARY.md` - Full project details
- `docs/BACKEND_ARCHITECTURE_GUIDE.md` - Architecture documentation
- `MOBILE_APP_TESTING_GUIDE.md` - Mobile testing guide

---

**Last Updated**: 2026-02-23  
**Status**: ✅ All services running with postgres/root credentials
