# 🚀 Getting Started Guide

Complete setup instructions for the Tisqra Platform.

## ⚙️ Prerequisites

### Required Software
- **Java 21** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Flutter SDK** (for mobile app) - [Download](https://flutter.dev/docs/get-started/install)
- **Git** - [Download](https://git-scm.com/downloads)

### Optional Tools
- **Postman** - For API testing
- **IntelliJ IDEA** or **VS Code** - For development

## 📋 Quick Start (5 Minutes)

### Step 1: Clone and Configure

```bash
cd Desktop/tisqra-platform
cp .env.example .env
```

### Step 2: Configure Environment Variables

Edit `.env` file with your credentials:

```bash
# PostgreSQL
POSTGRES_PASSWORD=your_secure_password

# Redis
REDIS_PASSWORD=your_redis_password

# Keycloak
KEYCLOAK_ADMIN_PASSWORD=your_keycloak_password

# Brevo SMTP (for emails)
BREVO_SMTP_USERNAME=your_brevo_username
BREVO_SMTP_PASSWORD=your_brevo_api_key
BREVO_SENDER_EMAIL=noreply@yourdomain.com

# Firebase (for push notifications) - Optional
FCM_CREDENTIALS_FILE=/app/config/firebase-credentials.json
```

### Step 3: Start Infrastructure Services

```bash
docker-compose up -d postgres redis kafka zookeeper keycloak
```

Wait ~30 seconds for services to be ready:

```bash
docker-compose ps
```

### Step 4: Build All Services

```bash
mvn clean install -DskipTests
```

### Step 5: Start All Microservices

```bash
docker-compose up --build
```

## 🌐 Access the Platform

Once all services are running:

| Service | URL | Credentials |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | - |
| **Eureka Dashboard** | http://localhost:8761 | - |
| **Keycloak Admin** | http://localhost:8180 | admin / (your password) |
| **Swagger API Docs** | http://localhost:8080/swagger-ui.html | - |

### Service Endpoints

- **User Service**: http://localhost:8081
- **Organization Service**: http://localhost:8082
- **Event Service**: http://localhost:8083
- **Order Service**: http://localhost:8084
- **Ticket Service**: http://localhost:8085
- **Payment Service**: http://localhost:8086
- **Notification Service**: http://localhost:8087
- **Analytics Service**: http://localhost:8088

## 🧪 Testing the Platform

### 1. Register a User

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User",
  "role": "GUEST"
}
```

### 2. Login

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

Response includes `accessToken` - use this for authenticated requests.

### 3. Create an Organization

```bash
POST http://localhost:8080/api/organizations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "My Event Company",
  "description": "We organize amazing events",
  "ownerId": "{userId}",
  "email": "info@mycompany.com",
  "subscriptionPlanCode": "FREE"
}
```

### 4. Create an Event

```bash
POST http://localhost:8080/api/events
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "organizationId": "{organizationId}",
  "name": "Tech Conference 2026",
  "description": "Annual tech conference",
  "category": "CONFERENCE",
  "startDate": "2026-06-15T09:00:00",
  "endDate": "2026-06-15T18:00:00",
  "location": {
    "name": "Convention Center",
    "address": "123 Main St",
    "city": "New York",
    "country": "USA"
  },
  "capacity": 500,
  "ticketCategories": [
    {
      "name": "General Admission",
      "price": 99.99,
      "quantity": 300
    }
  ]
}
```

### 5. Purchase Tickets

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}

{
  "userId": "{userId}",
  "eventId": "{eventId}",
  "items": [
    {
      "ticketCategoryId": "{categoryId}",
      "quantity": 2
    }
  ]
}
```

### 6. Process Payment

```bash
POST http://localhost:8080/api/payments/process
Authorization: Bearer {accessToken}

{
  "orderId": "{orderId}",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111"
}
```

**Note**: The mock payment gateway has a 90% success rate by default.

## 📱 Mobile App Setup

### Prerequisites

```bash
flutter doctor
```

### Run the App

```bash
cd mobile/tisqra_mobile_app
flutter pub get
flutter run
```

## 🐳 Docker Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-service
```

### Restart a Service

```bash
docker-compose restart user-service
```

### Stop All Services

```bash
docker-compose down
```

### Clean Everything (including volumes)

```bash
docker-compose down -v
```

## 🔍 Monitoring & Debugging

### Check Service Health

```bash
curl http://localhost:8081/actuator/health
```

### View Eureka Registry

Open http://localhost:8761 to see all registered services.

### Check Kafka Topics

```bash
docker exec -it eventticket-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Access PostgreSQL

```bash
docker exec -it eventticket-postgres psql -U eventticket_admin -d user_db
```

## 🎯 Default Users (Keycloak)

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | SUPER_ADMIN |
| organizer | organizer123 | ADMIN_ORG |
| scanner | scanner123 | SCANNER |
| guest | guest123 | GUEST |

## 🛠️ Development Mode

### Run Single Service Locally

```bash
cd services/user-service
mvn spring-boot:run
```

### Run with IDE

1. Import project in IntelliJ IDEA
2. Enable annotation processing (for Lombok & MapStruct)
3. Run individual service main classes

## 📊 Database Migrations

Flyway automatically runs migrations on startup. View migration history:

```bash
docker exec -it eventticket-postgres psql -U eventticket_admin -d user_db -c "SELECT * FROM flyway_schema_history;"
```

## 🔧 Troubleshooting

### Services won't start

```bash
# Check if ports are available
netstat -ano | findstr "8080"

# Check Docker resources
docker system df
```

### Database connection errors

```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check logs
docker-compose logs postgres
```

### Kafka connection errors

```bash
# Restart Kafka ecosystem
docker-compose restart zookeeper kafka
```

## 📚 Next Steps

1. ✅ Platform is running
2. 📖 Read the [API Documentation](http://localhost:8080/swagger-ui.html)
3. 🧪 Import Postman collection from `/postman`
4. 📱 Try the Flutter mobile app
5. 📊 Check the analytics dashboard

## 🆘 Need Help?

- Check the [README.md](README.md) for architecture overview
- Review service-specific documentation in each service folder
- Check Docker logs: `docker-compose logs -f [service-name]`

---

**🎉 You're all set! Happy coding!**
