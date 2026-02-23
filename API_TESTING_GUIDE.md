# 🧪 Tisqra Platform - API Testing Guide

## Quick Start

### Step 1: Import Postman Collection
1. Open **Postman**
2. Click **File** → **Import**
3. Select `postman/Tisqra-Complete-API.postman_collection.json`
4. Click **Import**

### Step 2: Get Authentication Token
1. Go to the **Authentication & Security** folder
2. Run **"Keycloak - Get Token"** request
3. Copy the `access_token` from the response
4. Click **Settings** (gear icon) → **Variables**
5. Paste the token into `access_token` variable

### Step 3: Test Endpoints
- All endpoints are organized by service
- Each service has health checks and Swagger UI links
- Replace placeholder IDs (like `1`, `2`) with actual values from your database

---

## 📚 Service Documentation Links

Click the **Swagger UI** request in each service folder to open interactive API documentation.

### Direct Swagger URLs

| Service | Swagger UI |
|---------|-----------|
| **User Service** | http://localhost:8081/swagger-ui.html |
| **Organization Service** | http://localhost:8082/swagger-ui.html |
| **Event Service** | http://localhost:8083/swagger-ui.html |
| **Order Service** | http://localhost:8084/swagger-ui.html |
| **Ticket Service** | http://localhost:8085/swagger-ui.html |
| **Payment Service** | http://localhost:8086/swagger-ui.html |
| **Notification Service** | http://localhost:8087/swagger-ui.html |
| **Analytics Service** | http://localhost:8088/swagger-ui.html |

---

## 🔐 Authentication

### Get Access Token (Keycloak OAuth2)

**Request:**
```http
POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=tisqra-client
username=admin
password=admin
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "..."
}
```

### Using Token in Requests
Add the token to the `Authorization` header:
```
Authorization: Bearer {access_token}
```

---

## 👤 User Service APIs

### Get All Users
```http
GET /api/v1/users
Authorization: Bearer {access_token}
```

### Create User
```http
POST /api/v1/users
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123!"
}
```

### Get User by ID
```http
GET /api/v1/users/{userId}
Authorization: Bearer {access_token}
```

### Update User
```http
PUT /api/v1/users/{userId}
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "email": "user@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

### Delete User
```http
DELETE /api/v1/users/{userId}
Authorization: Bearer {access_token}
```

---

## 🏢 Organization Service APIs

### Get All Organizations
```http
GET /api/v1/organizations
Authorization: Bearer {access_token}
```

### Create Organization
```http
POST /api/v1/organizations
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "name": "Tech Events Inc",
  "email": "info@techevents.com",
  "country": "US",
  "city": "San Francisco"
}
```

### Get Organization by ID
```http
GET /api/v1/organizations/{organizationId}
Authorization: Bearer {access_token}
```

---

## 🎭 Event Service APIs

### Get All Events
```http
GET /api/v1/events
Authorization: Bearer {access_token}
```

### Create Event
```http
POST /api/v1/events
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "title": "Tech Conference 2026",
  "description": "Annual technology conference",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-17T18:00:00Z",
  "location": "San Francisco Convention Center",
  "organizationId": 1,
  "capacity": 5000,
  "currency": "USD"
}
```

### Get Event by ID
```http
GET /api/v1/events/{eventId}
Authorization: Bearer {access_token}
```

---

## 📦 Order Service APIs

### Get All Orders
```http
GET /api/v1/orders
Authorization: Bearer {access_token}
```

### Create Order
```http
POST /api/v1/orders
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "userId": 1,
  "eventId": 1,
  "ticketQuantity": 2,
  "ticketType": "STANDARD"
}
```

### Get Order by ID
```http
GET /api/v1/orders/{orderId}
Authorization: Bearer {access_token}
```

---

## 🎫 Ticket Service APIs

### Get All Tickets
```http
GET /api/v1/tickets
Authorization: Bearer {access_token}
```

### Get Ticket by ID
```http
GET /api/v1/tickets/{ticketId}
Authorization: Bearer {access_token}
```

### Transfer Ticket
```http
POST /api/v1/tickets/{ticketId}/transfer
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "toUserId": 2,
  "transferMessage": "Enjoy the event!"
}
```

---

## 💳 Payment Service APIs

### Create Payment
```http
POST /api/v1/payments
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "orderId": 1,
  "amount": 199.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "cvv": "123"
}
```

### Get Payment by ID
```http
GET /api/v1/payments/{paymentId}
Authorization: Bearer {access_token}
```

---

## 📧 Notification Service APIs

### Send Email Notification
```http
POST /api/v1/notifications/email
Content-Type: application/json
Authorization: Bearer {access_token}

{
  "recipientEmail": "user@example.com",
  "subject": "Event Confirmation",
  "templateName": "ticket-purchase",
  "variables": {
    "eventName": "Tech Conference 2026",
    "ticketNumber": "TECH001"
  }
}
```

---

## 📈 Analytics Service APIs

### Get Sales Analytics
```http
GET /api/v1/analytics/sales
Authorization: Bearer {access_token}
```

### Get Event Analytics
```http
GET /api/v1/analytics/events
Authorization: Bearer {access_token}
```

---

## 🌐 API Gateway

### Health Check
```http
GET /actuator/health
```

### Gateway Routes
```http
GET /actuator/gateway/routes
```

---

## 🔍 Infrastructure Services

### Discovery Service (Eureka)
- **URL**: http://localhost:8761
- **Purpose**: View registered services and their instances

### Config Server
- **URL**: http://localhost:8888
- **Purpose**: View centralized configuration

---

## 📋 Testing Workflow

### 1. Create a Complete Flow
```
1. Create User
   ↓
2. Create Organization
   ↓
3. Create Event (in Organization)
   ↓
4. Create Order (User + Event)
   ↓
5. Create Payment (for Order)
   ↓
6. View Tickets (auto-created from Order)
   ↓
7. Transfer Ticket
   ↓
8. Send Notification
   ↓
9. View Analytics
```

### 2. Test Health Endpoints
Run all "Health Check" requests to verify services are up:
- User Service ✓
- Organization Service ✓
- Event Service ✓
- Order Service ✓
- Ticket Service ✓
- Payment Service ✓
- Notification Service ✓
- Analytics Service ✓

### 3. View Service Documentation
Click "Swagger UI" in each service folder to explore:
- All available endpoints
- Request/response schemas
- Parameter descriptions
- Try-it-out functionality

---

## 🛠️ Postman Tips

### 1. Set Environment Variables
Click **Settings** (gear icon) → **Variables** to set:
- `access_token`: OAuth2 token
- `base_url`: API Gateway URL

### 2. Use Variables in Requests
```
{{base_url}}/api/v1/users
{{access_token}}
```

### 3. Create Tests
Add tests to requests to validate responses:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
```

### 4. Run Collections
Use **Collection Runner** to execute multiple requests in sequence with data.

---

## 🔗 Useful Links

- **Postman Documentation**: https://learning.postman.com/
- **OpenAPI/Swagger Spec**: http://localhost:{port}/v3/api-docs
- **Keycloak Admin**: http://localhost:8180/admin
- **Docker Compose**: Check `docker-compose.yml` for service definitions

---

## ✅ Verification Checklist

- [ ] All services are running (`docker-compose ps`)
- [ ] Keycloak is accessible at http://localhost:8180
- [ ] Got access token from Keycloak
- [ ] Can hit health endpoints for all services
- [ ] Can create a user
- [ ] Can create an organization
- [ ] Can create an event
- [ ] Can create an order
- [ ] Can create a payment
- [ ] Swagger UI is accessible for all services

---

**Happy Testing! 🚀**
