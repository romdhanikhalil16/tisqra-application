# 🧪 Tisqra Platform - Postman Testing Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Testing Workflow](#testing-workflow)
4. [Service-by-Service Testing](#service-by-service-testing)
5. [Common Issues & Solutions](#common-issues--solutions)

---

## Prerequisites

### Required Software
- **Postman** (Desktop or Web version)
- **Docker Desktop** (for running the platform)
- **Java 17+** and **Maven 3.8+** (for building services)

### Before Testing
1. Start Docker Desktop
2. Run all infrastructure services:
   ```bash
   docker-compose up -d postgres redis kafka keycloak
   ```
3. Wait for Keycloak to be ready (http://localhost:8180)
4. Start the microservices in order (see workflow below)

---

## Environment Setup

### Postman Collection Import
1. Import the collection: `TISQRA_COMPLETE_API.postman_collection.json`
2. The collection includes all 50+ endpoints across all services

### Create Postman Environment Variables
Set up these variables in Postman:

| Variable | Value | Description |
|----------|-------|-------------|
| `base_url` | `http://localhost:8080` | API Gateway URL |
| `keycloak_url` | `http://localhost:8180` | Keycloak server URL |
| `realm` | `tisqra` | Keycloak realm name |
| `access_token` | (auto-populated) | JWT access token |
| `user_id` | (auto-populated) | Current user ID |
| `event_id` | (auto-populated) | Created event ID |
| `order_id` | (auto-populated) | Created order ID |

---

## Testing Workflow

### ⚡ Quick Start Testing Order

#### Phase 1: Infrastructure (Start First)
```bash
# Start infrastructure services
docker-compose up -d postgres redis kafka keycloak

# Wait 30 seconds for Keycloak initialization
```

#### Phase 2: Core Services (Start in Order)
```bash
# 1. Discovery Service (Port 8761)
cd infrastructure/discovery-service
mvn spring-boot:run

# 2. Config Server (Port 8888) - Wait 10 seconds
cd infrastructure/config-server
mvn spring-boot:run

# 3. API Gateway (Port 8080) - Wait 10 seconds
cd infrastructure/api-gateway
mvn spring-boot:run

# 4. User Service (Port 8081) - Wait 10 seconds
cd services/user-service
mvn spring-boot:run
```

#### Phase 3: Business Services (Can Start in Parallel)
```bash
# Event Service (Port 8082)
cd services/event-service
mvn spring-boot:run

# Order Service (Port 8083)
cd services/order-service
mvn spring-boot:run

# Payment Service (Port 8084)
cd services/payment-service
mvn spring-boot:run

# Notification Service (Port 8088)
cd services/notification-service
mvn spring-boot:run
```

#### Phase 4: Verify All Services Running
Check Eureka Dashboard: http://localhost:8761

Expected registered services:
- API-GATEWAY
- USER-SERVICE
- EVENT-SERVICE
- ORDER-SERVICE
- PAYMENT-SERVICE
- NOTIFICATION-SERVICE

---

## Service-by-Service Testing

### 🔐 1. User Service (Authentication)

**Test Order:**

#### 1.1 Register New User
```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "email": "testuser@gmail.com",
  "password": "Test@1234",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "+1234567890"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "uuid-here",
  "email": "testuser@gmail.com",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "+1234567890",
  "emailVerified": false,
  "active": true,
  "createdAt": "2026-03-24T09:30:00"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("user_id", jsonData.id);
    pm.test("User registered successfully", function() {
        pm.expect(jsonData.email).to.eql("testuser@gmail.com");
    });
}
```

#### 1.2 Login User
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "testuser@gmail.com",
  "password": "Test@1234"
}
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "refresh-token-here",
  "user": {
    "id": "uuid",
    "email": "testuser@gmail.com",
    "firstName": "Test",
    "lastName": "User"
  }
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.test("Login successful", function() {
        pm.expect(jsonData.tokenType).to.eql("Bearer");
        pm.expect(jsonData.accessToken).to.be.a('string');
    });
}
```

#### 1.3 Get User Profile (Authenticated)
```http
GET {{base_url}}/api/users/{{user_id}}
Authorization: Bearer {{access_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": "uuid",
  "email": "testuser@gmail.com",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "+1234567890",
  "emailVerified": false,
  "active": true
}
```

#### 1.4 Update User Profile
```http
PUT {{base_url}}/api/users/{{user_id}}
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "firstName": "Updated",
  "lastName": "Name",
  "phoneNumber": "+9876543210"
}
```

#### 1.5 Request Password Reset
```http
POST {{base_url}}/api/auth/password/reset-request?email=testuser@gmail.com
```

**Expected Response (200 OK):**
- Empty body
- Check notification service logs for email sent

---

### 🎫 2. Event Service

**Prerequisites:** Must be logged in as ADMIN_ORG or SUPER_ADMIN

#### 2.1 Create Event
```http
POST {{base_url}}/api/events
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "organizationId": "{{org_id}}",
  "title": "Summer Music Festival 2026",
  "description": "The biggest music festival of the year",
  "category": "MUSIC",
  "location": {
    "venueName": "Central Park",
    "address": "Central Park, New York",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10024",
    "latitude": 40.785091,
    "longitude": -73.968285
  },
  "startDate": "2026-07-15T18:00:00",
  "endDate": "2026-07-17T23:00:00",
  "capacity": 5000,
  "ticketCategories": [
    {
      "name": "General Admission",
      "description": "Standard entry ticket",
      "price": 99.99,
      "quantity": 3000,
      "maxPerOrder": 10
    },
    {
      "name": "VIP",
      "description": "VIP access with exclusive perks",
      "price": 299.99,
      "quantity": 500,
      "maxPerOrder": 4
    }
  ],
  "schedules": [
    {
      "title": "Day 1 - Opening Night",
      "description": "Festival opens with headliner performance",
      "startTime": "2026-07-15T18:00:00",
      "endTime": "2026-07-15T23:00:00"
    }
  ]
}
```

**Expected Response (201 Created):**
```json
{
  "id": "event-uuid",
  "organizationId": "org-uuid",
  "title": "Summer Music Festival 2026",
  "slug": "summer-music-festival-2026",
  "description": "The biggest music festival of the year",
  "category": "MUSIC",
  "status": "DRAFT",
  "location": { ... },
  "ticketCategories": [ ... ],
  "schedules": [ ... ],
  "createdAt": "2026-03-24T09:30:00"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("event_id", jsonData.id);
    pm.test("Event created successfully", function() {
        pm.expect(jsonData.status).to.eql("DRAFT");
        pm.expect(jsonData.ticketCategories).to.have.lengthOf(2);
    });
}
```

#### 2.2 Get Event by ID
```http
GET {{base_url}}/api/events/{{event_id}}
```

#### 2.3 Get Event by Slug
```http
GET {{base_url}}/api/events/slug/summer-music-festival-2026
```

#### 2.4 Search Events
```http
GET {{base_url}}/api/events/search?query=music&page=0&size=10
```

#### 2.5 Get Upcoming Events
```http
GET {{base_url}}/api/events/upcoming?page=0&size=20
```

#### 2.6 Get Events by Category
```http
GET {{base_url}}/api/events/category/MUSIC?page=0&size=10
```

**Available Categories:**
- MUSIC
- SPORTS
- ARTS
- THEATER
- CONFERENCE
- WORKSHOP
- FESTIVAL
- OTHER

#### 2.7 Publish Event
```http
POST {{base_url}}/api/events/{{event_id}}/publish
Authorization: Bearer {{access_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": "event-uuid",
  "status": "PUBLISHED",
  ...
}
```

#### 2.8 Create Promo Code
```http
POST {{base_url}}/api/promo-codes
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "eventId": "{{event_id}}",
  "code": "SUMMER2026",
  "discountType": "PERCENTAGE",
  "discountValue": 20.0,
  "maxUses": 100,
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-07-14T23:59:59"
}
```

#### 2.9 Validate Promo Code
```http
GET {{base_url}}/api/promo-codes/validate?code=SUMMER2026&eventId={{event_id}}
```

---

### 🛒 3. Order Service

#### 3.1 Create Order
```http
POST {{base_url}}/api/orders
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "userId": "{{user_id}}",
  "eventId": "{{event_id}}",
  "items": [
    {
      "ticketCategoryId": "general-admission-uuid",
      "quantity": 2,
      "unitPrice": 99.99
    },
    {
      "ticketCategoryId": "vip-uuid",
      "quantity": 1,
      "unitPrice": 299.99
    }
  ],
  "promoCode": "SUMMER2026",
  "customerEmail": "testuser@gmail.com",
  "customerPhone": "+1234567890"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "order-uuid",
  "orderNumber": "ORD-20260324-001",
  "userId": "user-uuid",
  "eventId": "event-uuid",
  "status": "PENDING",
  "items": [
    {
      "ticketCategoryId": "uuid",
      "quantity": 2,
      "unitPrice": 99.99,
      "subtotal": 199.98
    }
  ],
  "subtotal": 499.97,
  "discount": 99.99,
  "total": 399.98,
  "createdAt": "2026-03-24T09:30:00"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("order_id", jsonData.id);
    pm.environment.set("order_total", jsonData.total);
    pm.test("Order created successfully", function() {
        pm.expect(jsonData.status).to.eql("PENDING");
        pm.expect(jsonData.total).to.be.below(jsonData.subtotal);
    });
}
```

#### 3.2 Get Order by ID
```http
GET {{base_url}}/api/orders/{{order_id}}
Authorization: Bearer {{access_token}}
```

#### 3.3 Get User Orders
```http
GET {{base_url}}/api/orders/user/{{user_id}}?page=0&size=10
Authorization: Bearer {{access_token}}
```

#### 3.4 Get Order by Order Number
```http
GET {{base_url}}/api/orders/number/ORD-20260324-001
Authorization: Bearer {{access_token}}
```

---

### 💳 4. Payment Service

#### 4.1 Process Payment
```http
POST {{base_url}}/api/payments/process
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "orderId": "{{order_id}}",
  "amount": {{order_total}},
  "paymentMethod": "CREDIT_CARD",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "cardHolderName": "Test User",
    "expiryMonth": "12",
    "expiryYear": "2028",
    "cvv": "123"
  }
}
```

**Expected Response (201 Created):**
```json
{
  "id": "payment-uuid",
  "orderId": "order-uuid",
  "amount": 399.98,
  "currency": "USD",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "providerPaymentId": "mock-payment-12345",
  "processedAt": "2026-03-24T09:30:00"
}
```

**Postman Test Script:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("payment_id", jsonData.id);
    pm.test("Payment processed successfully", function() {
        pm.expect(jsonData.status).to.eql("SUCCESS");
        pm.expect(jsonData.amount).to.eql(pm.environment.get("order_total"));
    });
}
```

**Note:** This uses a mock payment gateway. All payments will succeed with card number starting with "4".

#### 4.2 Get Payment by ID
```http
GET {{base_url}}/api/payments/{{payment_id}}
Authorization: Bearer {{access_token}}
```

#### 4.3 Get Payment by Order ID
```http
GET {{base_url}}/api/payments/order/{{order_id}}
Authorization: Bearer {{access_token}}
```

#### 4.4 Process Refund (Admin Only)
```http
POST {{base_url}}/api/payments/{{payment_id}}/refund
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "amount": 399.98,
  "reason": "Customer requested refund"
}
```

**Expected Response (200 OK):**
- Empty body
- Payment status updated to REFUNDED

---

### 🔔 5. Notification Service

**Note:** This service is event-driven. Notifications are automatically sent when:
- User registers (email verification)
- Password reset requested
- Order confirmed (ticket purchase confirmation)
- Payment successful (payment receipt)

#### 5.1 Get User Notifications
```http
GET {{base_url}}/api/notifications/user/{{user_id}}?page=0&size=10
Authorization: Bearer {{access_token}}
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": "notification-uuid",
      "userId": "user-uuid",
      "type": "EMAIL",
      "subject": "Welcome to Tisqra!",
      "message": "Your account has been created successfully",
      "status": "SENT",
      "sentAt": "2026-03-24T09:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

#### 5.2 Mark Notification as Read
```http
POST {{base_url}}/api/notifications/{{notification_id}}/read
Authorization: Bearer {{access_token}}
```

---

## Complete Testing Flow

### End-to-End User Journey

Follow this sequence to test the complete user flow:

```
1. Register User → Save user_id and access_token
2. Login User → Update access_token
3. Create Event (as Admin) → Save event_id
4. Publish Event
5. Create Promo Code
6. Browse Events (search, upcoming, by category)
7. Create Order → Save order_id
8. Process Payment → Verify order status changes to CONFIRMED
9. Get User Orders → Verify order appears
10. Check Notifications → Verify email notifications sent
```

### Expected Kafka Events

Throughout the flow, these Kafka events should be published:

| Event | Topic | Triggered By |
|-------|-------|--------------|
| UserRegisteredEvent | user-events | User registration |
| EventCreatedEvent | event-events | Event creation |
| EventPublishedEvent | event-events | Event publish |
| OrderCreatedEvent | order-events | Order creation |
| PaymentProcessedEvent | payment-events | Payment success |
| TicketPurchasedEvent | ticket-events | Order confirmation |

---

## Common Issues & Solutions

### Issue 1: 401 Unauthorized
**Problem:** API returns 401 even with valid token

**Solutions:**
1. Check if token is expired (default: 1 hour)
2. Verify token format in Postman: `Bearer {{access_token}}`
3. Re-login to get fresh token
4. Check Keycloak is running: http://localhost:8180

### Issue 2: Connection Refused
**Problem:** Cannot connect to service

**Solutions:**
1. Verify service is running: Check Eureka dashboard
2. Check correct port is used
3. Ensure API Gateway is running (all requests go through port 8080)
4. Check Docker containers: `docker ps`

### Issue 3: Service Not Registered in Eureka
**Problem:** Service not showing in Eureka dashboard

**Solutions:**
1. Wait 30 seconds after service start
2. Check service logs for connection errors
3. Verify Eureka is running on port 8761
4. Check application.yml eureka configuration

### Issue 4: Database Connection Error
**Problem:** Service fails to start with DB error

**Solutions:**
1. Verify PostgreSQL is running: `docker ps | grep postgres`
2. Check database exists: Each service has its own DB
3. Verify credentials in application.yml (default: postgres/root)
4. Run Flyway migrations: `mvn flyway:migrate`

### Issue 5: Event Not Found (404)
**Problem:** Cannot find created event

**Solutions:**
1. Verify event_id is correct
2. Check if event was created successfully (201 response)
3. Ensure using correct endpoint (/api/events/{id})
4. Check API Gateway routing

### Issue 6: Promo Code Not Applied
**Problem:** Discount not calculated in order

**Solutions:**
1. Verify promo code is valid and not expired
2. Check maxUses hasn't been reached
3. Ensure promo code matches event
4. Validate discountType and discountValue are correct

### Issue 7: Kafka Events Not Publishing
**Problem:** Notifications not received

**Solutions:**
1. Check Kafka is running: `docker ps | grep kafka`
2. Verify Kafka bootstrap servers in application.yml
3. Check service logs for Kafka connection errors
4. Ensure topics are created automatically

---

## Testing Tips

### 1. Use Postman Collections
- Organize requests by service
- Use folders for different flows
- Add request descriptions

### 2. Environment Variables
Always use variables for:
- `{{base_url}}` - Easy to switch environments
- `{{access_token}}` - Auto-refresh tokens
- `{{user_id}}`, `{{event_id}}`, `{{order_id}}` - Chain requests

### 3. Test Scripts
Add validation scripts to each request:
```javascript
pm.test("Status code is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("Response has required fields", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('createdAt');
});
```

### 4. Pre-request Scripts
For automatic token refresh:
```javascript
const tokenExpiry = pm.environment.get("token_expiry");
const now = new Date().getTime();

if (!tokenExpiry || now >= tokenExpiry) {
    // Re-login to get new token
    pm.sendRequest({
        url: pm.environment.get("base_url") + "/api/auth/login",
        method: 'POST',
        header: 'Content-Type:application/json',
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: "testuser@gmail.com",
                password: "Test@1234"
            })
        }
    }, function(err, res) {
        if (!err) {
            var jsonData = res.json();
            pm.environment.set("access_token", jsonData.accessToken);
            pm.environment.set("token_expiry", now + (jsonData.expiresIn * 1000));
        }
    });
}
```

### 5. Monitor Service Health
Before testing, check health endpoints:
```http
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
GET http://localhost:8082/actuator/health
```

---

## Service Ports Reference

| Service | Port | Health Check |
|---------|------|--------------|
| API Gateway | 8080 | http://localhost:8080/actuator/health |
| User Service | 8081 | http://localhost:8081/actuator/health |
| Event Service | 8082 | http://localhost:8082/actuator/health |
| Order Service | 8083 | http://localhost:8083/actuator/health |
| Payment Service | 8084 | http://localhost:8084/actuator/health |
| Notification Service | 8088 | http://localhost:8088/actuator/health |
| Discovery Service | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888/actuator/health |
| Keycloak | 8180 | http://localhost:8180 |
| PostgreSQL | 5432 | - |
| Redis | 6379 | - |
| Kafka | 9092 | - |

---

## Next Steps

After completing this testing guide:
1. Review [Keycloak Concepts Guide](02_KEYCLOAK_CONCEPTS.md) for authentication details
2. Review [Technologies Overview](03_TECHNOLOGIES_OVERVIEW.md) for architecture understanding
3. Explore advanced features like analytics and ticket validation
4. Set up CI/CD pipelines for automated testing

---

**Happy Testing! 🚀**
