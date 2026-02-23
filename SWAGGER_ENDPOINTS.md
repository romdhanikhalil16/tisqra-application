# 📖 Tisqra Platform - Swagger/OpenAPI Documentation

## 🚀 Quick Access to All Service Documentation

Click any link below to open the interactive Swagger UI for that service:

### Core Infrastructure
| Service | Swagger UI | Health Check | Base URL |
|---------|-----------|--------------|----------|
| **API Gateway** | N/A (Router) | http://localhost:8080/actuator/health | http://localhost:8080 |
| **Discovery Service** | N/A (Eureka) | http://localhost:8761 | http://localhost:8761 |
| **Config Server** | N/A (Config) | http://localhost:8888/actuator/health | http://localhost:8888 |

### Business Microservices
| Service | Port | Swagger UI | Health | OpenAPI Spec |
|---------|------|-----------|--------|--------------|
| **User Service** | 8081 | [Open Swagger](http://localhost:8081/swagger-ui.html) | [Check](http://localhost:8081/actuator/health) | [JSON](http://localhost:8081/v3/api-docs) |
| **Organization Service** | 8082 | [Open Swagger](http://localhost:8082/swagger-ui.html) | [Check](http://localhost:8082/actuator/health) | [JSON](http://localhost:8082/v3/api-docs) |
| **Event Service** | 8083 | [Open Swagger](http://localhost:8083/swagger-ui.html) | [Check](http://localhost:8083/actuator/health) | [JSON](http://localhost:8083/v3/api-docs) |
| **Order Service** | 8084 | [Open Swagger](http://localhost:8084/swagger-ui.html) | [Check](http://localhost:8084/actuator/health) | [JSON](http://localhost:8084/v3/api-docs) |
| **Ticket Service** | 8085 | [Open Swagger](http://localhost:8085/swagger-ui.html) | [Check](http://localhost:8085/actuator/health) | [JSON](http://localhost:8085/v3/api-docs) |
| **Payment Service** | 8086 | [Open Swagger](http://localhost:8086/swagger-ui.html) | [Check](http://localhost:8086/actuator/health) | [JSON](http://localhost:8086/v3/api-docs) |
| **Notification Service** | 8087 | [Open Swagger](http://localhost:8087/swagger-ui.html) | [Check](http://localhost:8087/actuator/health) | [JSON](http://localhost:8087/v3/api-docs) |
| **Analytics Service** | 8088 | [Open Swagger](http://localhost:8088/swagger-ui.html) | [Check](http://localhost:8088/actuator/health) | [JSON](http://localhost:8088/v3/api-docs) |

---

## 📝 OpenAPI/Swagger Specification Files

### Download OpenAPI Specs
Each service exposes its OpenAPI specification at `/v3/api-docs`:

```bash
# Download all specs
curl http://localhost:8081/v3/api-docs > user-service-openapi.json
curl http://localhost:8082/v3/api-docs > organization-service-openapi.json
curl http://localhost:8083/v3/api-docs > event-service-openapi.json
curl http://localhost:8084/v3/api-docs > order-service-openapi.json
curl http://localhost:8085/v3/api-docs > ticket-service-openapi.json
curl http://localhost:8086/v3/api-docs > payment-service-openapi.json
curl http://localhost:8087/v3/api-docs > notification-service-openapi.json
curl http://localhost:8088/v3/api-docs > analytics-service-openapi.json
```

---

## 🔗 Available Swagger UI Endpoints

### Using Swagger UI Online Editor
You can use the official Swagger Editor to view and test your APIs:

**Method 1: Online Swagger Editor**
1. Visit https://editor.swagger.io/
2. File → Import URL
3. Enter: `http://localhost:8081/v3/api-docs` (or any other service)
4. Explore and test endpoints

**Method 2: Local Swagger UI**
- Just open each service's Swagger URL in your browser
- No additional tools needed

---

## 🎯 API Endpoints Summary

### User Service (8081)
**Base Path**: `/api/v1/users`

```
GET    /                    - Get all users
POST   /                    - Create new user
GET    /{userId}            - Get user by ID
PUT    /{userId}            - Update user
DELETE /{userId}            - Delete user
GET    /{userId}/profile    - Get user profile
PUT    /{userId}/password   - Change password
```

### Organization Service (8082)
**Base Path**: `/api/v1/organizations`

```
GET    /                              - Get all organizations
POST   /                              - Create organization
GET    /{organizationId}              - Get organization by ID
PUT    /{organizationId}              - Update organization
DELETE /{organizationId}              - Delete organization
GET    /{organizationId}/members      - Get organization members
POST   /{organizationId}/members      - Add member to organization
GET    /{organizationId}/subscriptions - Get subscriptions
```

### Event Service (8083)
**Base Path**: `/api/v1/events`

```
GET    /                       - Get all events
POST   /                       - Create event
GET    /{eventId}              - Get event by ID
PUT    /{eventId}              - Update event
DELETE /{eventId}              - Delete event
GET    /{eventId}/categories   - Get event ticket categories
POST   /{eventId}/categories   - Create ticket category
GET    /{eventId}/schedules    - Get event schedules
POST   /{eventId}/promo-codes  - Create promo code
```

### Order Service (8084)
**Base Path**: `/api/v1/orders`

```
GET    /                   - Get all orders
POST   /                   - Create order
GET    /{orderId}          - Get order by ID
PUT    /{orderId}          - Update order
DELETE /{orderId}          - Cancel order
GET    /{orderId}/items    - Get order items
POST   /{orderId}/items    - Add item to order
```

### Ticket Service (8085)
**Base Path**: `/api/v1/tickets`

```
GET    /                        - Get all tickets
GET    /{ticketId}              - Get ticket by ID
POST   /{ticketId}/validate     - Validate ticket
POST   /{ticketId}/transfer     - Transfer ticket
POST   /{ticketId}/qr-code      - Generate QR code
GET    /{ticketId}/qr-code      - Get QR code
GET    /user/{userId}           - Get tickets for user
```

### Payment Service (8086)
**Base Path**: `/api/v1/payments`

```
GET    /                    - Get all payments
POST   /                    - Create payment
GET    /{paymentId}         - Get payment by ID
POST   /{paymentId}/refund  - Refund payment
GET    /{paymentId}/status  - Get payment status
GET    /invoice/{invoiceId} - Get invoice
```

### Notification Service (8087)
**Base Path**: `/api/v1/notifications`

```
POST   /email                     - Send email notification
POST   /sms                       - Send SMS notification
GET    /                          - Get notifications
GET    /{notificationId}          - Get notification by ID
PUT    /{notificationId}/read     - Mark as read
GET    /templates                 - Get email templates
POST   /templates                 - Create custom template
```

### Analytics Service (8088)
**Base Path**: `/api/v1/analytics`

```
GET    /sales                 - Get sales analytics
GET    /events                - Get event analytics
GET    /users                 - Get user analytics
GET    /revenue               - Get revenue analytics
GET    /tickets               - Get ticket analytics
GET    /dashboard             - Get dashboard metrics
```

---

## 🔐 Authentication & Security

### OAuth2/OpenID Connect
All endpoints (except health checks) require authentication via OAuth2 Bearer token.

**Get Token:**
```bash
curl -X POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tisqra-client" \
  -d "username=admin" \
  -d "password=admin"
```

**Use Token in Requests:**
```bash
curl http://localhost:8081/api/v1/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 📊 Popular Test Scenarios

### Scenario 1: Complete Event Booking Flow
```
1. Create User (User Service)
   POST /api/v1/users
   
2. Create Organization (Organization Service)
   POST /api/v1/organizations
   
3. Create Event (Event Service)
   POST /api/v1/events
   
4. Create Order (Order Service)
   POST /api/v1/orders
   
5. Create Payment (Payment Service)
   POST /api/v1/payments
   
6. View Tickets (Ticket Service)
   GET /api/v1/tickets/user/{userId}
   
7. Transfer Ticket (Ticket Service)
   POST /api/v1/tickets/{ticketId}/transfer
   
8. Get Analytics (Analytics Service)
   GET /api/v1/analytics/sales
```

### Scenario 2: Ticket Validation at Event
```
1. Validate Ticket (Ticket Service)
   POST /api/v1/tickets/{ticketId}/validate
   
2. Get QR Code (Ticket Service)
   GET /api/v1/tickets/{ticketId}/qr-code
   
3. Log Entry (Analytics Service)
   GET /api/v1/analytics/tickets
```

---

## 🧪 Testing with Different Tools

### curl (Command Line)
```bash
# Get all users
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/api/v1/users

# Create user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Postman
- Import `postman/Tisqra-Complete-API.postman_collection.json`
- See `API_TESTING_GUIDE.md` for details

### IntelliJ IDEA / VS Code
- Built-in REST client support
- Use `.http` files with REST Client plugin
- Supports OpenAPI documentation

### Python (requests)
```python
import requests

token = "YOUR_ACCESS_TOKEN"
headers = {"Authorization": f"Bearer {token}"}

# Get all users
response = requests.get(
    "http://localhost:8081/api/v1/users",
    headers=headers
)
print(response.json())
```

---

## 🐛 Troubleshooting Swagger

### Swagger UI Not Loading
1. Check if service is running: `docker-compose ps`
2. Check service logs: `docker-compose logs service-name`
3. Verify port is accessible: `curl http://localhost:PORT/actuator/health`

### CORS Issues
- Services are configured to allow cross-origin requests
- Check `application.yml` if issues persist

### Cannot Get OpenAPI Spec
```bash
# Verify spec is available
curl http://localhost:8081/v3/api-docs | jq .
```

---

## 📚 Additional Resources

### Official Documentation
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Swagger/OpenAPI 3.0](https://swagger.io/specification/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)

### Postman Collections
- Main Collection: `postman/Tisqra-Platform.postman_collection.json`
- Advanced Endpoints: `postman/Advanced-Endpoints.postman_collection.json`
- Complete Collection: `postman/Tisqra-Complete-API.postman_collection.json`

---

## ✅ Verification Steps

Run this in your terminal to verify all Swagger endpoints are accessible:

```bash
#!/bin/bash

echo "Checking Swagger UI endpoints..."

services=(
  "8081:user-service"
  "8082:organization-service"
  "8083:event-service"
  "8084:order-service"
  "8085:ticket-service"
  "8086:payment-service"
  "8087:notification-service"
  "8088:analytics-service"
)

for service in "${services[@]}"; do
  PORT=$(echo $service | cut -d: -f1)
  NAME=$(echo $service | cut -d: -f2)
  
  echo -n "Testing $NAME (Port $PORT)... "
  if curl -s http://localhost:$PORT/swagger-ui.html > /dev/null; then
    echo "✅ OK"
  else
    echo "❌ NOT AVAILABLE"
  fi
done
```

---

**Last Updated**: 2026-02-23  
**All Services Online**: ✅
