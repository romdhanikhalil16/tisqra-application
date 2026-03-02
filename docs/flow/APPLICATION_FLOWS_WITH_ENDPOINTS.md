# 🔄 Application Flows with Endpoints

## Complete Application Architecture & Endpoint Mapping

This document provides a comprehensive overview of the Tisqra Platform application flows with all endpoints mapped in the correct testing order.

---

## 📐 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      TISQRA PLATFORM                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐         ┌──────────────┐                      │
│  │ Flutter App  │         │  Web Browser │                      │
│  └──────┬───────┘         └──────┬───────┘                      │
│         │                        │                               │
│         └────────────┬───────────┘                              │
│                      │                                           │
│            ┌─────────▼──────────┐                               │
│            │  API GATEWAY       │  (Port 8080)                  │
│            │  nginx/Spring Cloud│                               │
│            └────────┬───────────┘                               │
│                     │                                            │
│        ┌────────────┼────────────┐                              │
│        │            │            │                              │
│   ┌────▼──┐   ┌────▼──┐    ┌───▼────┐                          │
│   │User   │   │Org    │    │Event   │  ... other services      │
│   │Service│   │Service│    │Service │                          │
│   │(8081) │   │(8082) │    │(8083)  │                          │
│   └────┬──┘   └────┬──┘    └───┬────┘                          │
│        │           │            │                              │
│        └───────────┼────────────┘                              │
│                    │                                            │
│            ┌───────▼────────┐                                  │
│            │  PostgreSQL    │  Shared Database                │
│            │  Redis         │  Cache                          │
│            │  Kafka         │  Message Queue                  │
│            └────────────────┘                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Authentication Flow

All endpoints require OAuth2 authentication tokens except:
- `POST /api/v1/users/register`
- `POST /api/v1/users/verify-email`
- Health check endpoints

### Token Generation Flow

```
1. User calls: POST /api/v1/users/login
   Input: { email, password }
   Output: { access_token, refresh_token, expires_in }

2. Token structure:
   Authorization: Bearer {access_token}

3. Token expires in: 30 minutes
   
4. To refresh:
   POST /api/v1/users/refresh-token
   Input: { refresh_token }
   Output: { access_token, refresh_token }
```

---

## 🌊 Complete Application Flows

### FLOW 1: USER REGISTRATION & EMAIL VERIFICATION

**Service**: User Service (8081)  
**Duration**: ~5 endpoints  
**Roles**: All (Guest, Admin, Scanner)

#### Step 1: Register User
```
POST /api/v1/users/register
Headers: Content-Type: application/json
Body: {
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+1234567890"
}
Response: 201 Created
{
  "id": "user-123",
  "email": "john@example.com",
  "emailVerified": false,
  "createdAt": "2026-02-25T10:00:00Z"
}
```

#### Step 2: Verify Email
```
POST /api/v1/users/verify-email
Headers: Content-Type: application/json
Body: {
  "email": "john@example.com",
  "verificationCode": "123456"  // Sent via email
}
Response: 200 OK
{
  "message": "Email verified successfully",
  "emailVerified": true
}
```

#### Step 3: Login User
```
POST /api/v1/users/login
Headers: Content-Type: application/json
Body: {
  "email": "john@example.com",
  "password": "SecurePass123!"
}
Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-xyz",
  "expiresIn": 1800,
  "userId": "user-123",
  "roles": ["ROLE_GUEST"]
}
```

---

### FLOW 2: SUPERADMIN OPERATIONS

**Service**: User Service (8081), Organization Service (8082), Analytics Service (8088)  
**Duration**: ~20 endpoints  
**Roles**: SuperAdmin only

#### Step 1: SuperAdmin Login
```
POST /api/v1/users/login
Body: {
  "email": "superadmin@tisqra.com",
  "password": "SuperAdminPass123!"
}
Response: 200 OK
{
  "accessToken": "...",
  "roles": ["ROLE_SUPERADMIN"],
  "userId": "superadmin-1"
}
```

#### Step 2: Get Dashboard Data
```
GET /api/v1/analytics/dashboard
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "totalOrganizations": 45,
  "totalUsers": 5000,
  "totalRevenue": 125000.00,
  "activeEvents": 89,
  "chartData": {...}
}
```

#### Step 3: Create Organization
```
POST /api/v1/organizations
Headers: Authorization: Bearer {accessToken}
Body: {
  "name": "Event Company XYZ",
  "description": "Events organizing company",
  "email": "admin@eventxyz.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA"
}
Response: 201 Created
{
  "id": "org-456",
  "name": "Event Company XYZ",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

#### Step 4: List All Organizations
```
GET /api/v1/organizations
Headers: Authorization: Bearer {accessToken}
Query: ?page=0&size=20&sort=createdAt,desc
Response: 200 OK
{
  "content": [{...}, {...}],
  "totalElements": 45,
  "totalPages": 3,
  "currentPage": 0
}
```

#### Step 5: View Organization Details
```
GET /api/v1/organizations/{orgId}
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "id": "org-456",
  "name": "Event Company XYZ",
  "email": "admin@eventxyz.com",
  "subscriptionPlan": "PREMIUM",
  "createdAt": "2026-02-25T10:00:00Z",
  "stats": {
    "eventsCreated": 12,
    "ticketsSold": 450,
    "totalRevenue": 15000.00
  }
}
```

#### Step 6: Get System Analytics
```
GET /api/v1/analytics/dashboard
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "systemMetrics": {
    "totalOrganizations": 45,
    "totalUsers": 5000,
    "totalRevenue": 125000.00,
    "activeEvents": 89,
    "ticketsSold": 12000,
    "averageTicketPrice": 10.42
  },
  "chartData": {...}
}
```

---

### FLOW 3: ORGANIZATION ADMIN - CREATE & MANAGE EVENT

**Service**: Organization Service (8082), Event Service (8083), Order Service (8084), Analytics Service (8088)  
**Duration**: ~15 endpoints  
**Roles**: Organization Admin

#### Step 1: Admin Login
```
POST /api/v1/users/login
Body: {
  "email": "admin@eventxyz.com",
  "password": "AdminPass123!"
}
Response: 200 OK
{
  "accessToken": "...",
  "roles": ["ROLE_ADMIN_ORG"],
  "organizationId": "org-456"
}
```

#### Step 2: Get Organization Profile
```
GET /api/v1/organizations/{orgId}
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "id": "org-456",
  "name": "Event Company XYZ",
  "subscription": {
    "plan": "PREMIUM",
    "maxEvents": 100,
    "maxTicketsPerEvent": 10000,
    "expiresAt": "2026-12-31"
  }
}
```

#### Step 3: Create Event
```
POST /api/v1/events
Headers: Authorization: Bearer {accessToken}
Body: {
  "title": "Tech Conference 2026",
  "description": "Annual tech conference",
  "organizationId": "org-456",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-15T17:00:00Z",
  "location": "Convention Center, NY",
  "capacity": 500,
  "status": "DRAFT"
}
Response: 201 Created
{
  "id": "event-789",
  "title": "Tech Conference 2026",
  "status": "DRAFT",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

#### Step 4: Create Ticket Categories
```
POST /api/v1/ticket-categories
Headers: Authorization: Bearer {accessToken}
Body: {
  "eventId": "event-789",
  "name": "VIP Pass",
  "description": "Premium access",
  "quantity": 50,
  "price": 500.00,
  "benefits": ["Priority Seating", "Lunch Included", "Meet & Greet"]
}
Response: 201 Created
{
  "id": "category-101",
  "name": "VIP Pass",
  "quantity": 50,
  "available": 50,
  "price": 500.00
}
```

#### Step 5: Create More Ticket Categories
```
POST /api/v1/ticket-categories
Headers: Authorization: Bearer {accessToken}
Body: {
  "eventId": "event-789",
  "name": "Standard Pass",
  "description": "Regular access",
  "quantity": 300,
  "price": 100.00
}
Response: 201 Created
{
  "id": "category-102",
  "name": "Standard Pass",
  "quantity": 300,
  "available": 300,
  "price": 100.00
}
```

#### Step 6: Create Promo Code
```
POST /api/v1/promo-codes
Headers: Authorization: Bearer {accessToken}
Body: {
  "eventId": "event-789",
  "code": "EARLYBIRD20",
  "discount": 20,
  "discountType": "PERCENTAGE",
  "maxUses": 100,
  "expiryDate": "2026-06-01T23:59:59Z",
  "applicableCategories": ["category-101", "category-102"]
}
Response: 201 Created
{
  "id": "promo-201",
  "code": "EARLYBIRD20",
  "status": "ACTIVE"
}
```

#### Step 7: Publish Event
```
PUT /api/v1/events/{eventId}
Headers: Authorization: Bearer {accessToken}
Body: {
  "status": "PUBLISHED"
}
Response: 200 OK
{
  "id": "event-789",
  "status": "PUBLISHED",
  "publishedAt": "2026-02-25T10:00:00Z"
}
```

#### Step 8: View Orders for Event
```
GET /api/v1/orders/organization/{orgId}
Headers: Authorization: Bearer {accessToken}
Query: ?eventId=event-789&page=0&size=20
Response: 200 OK
{
  "content": [
    {
      "id": "order-1001",
      "userId": "user-123",
      "eventId": "event-789",
      "totalAmount": 500.00,
      "status": "COMPLETED",
      "createdAt": "2026-02-25T11:00:00Z"
    },
    {...}
  ],
  "totalElements": 25
}
```

#### Step 9: View Tickets Sold
```
GET /api/v1/tickets/organization/{orgId}
Headers: Authorization: Bearer {accessToken}
Query: ?eventId=event-789
Response: 200 OK
{
  "content": [
    {
      "id": "ticket-1",
      "eventId": "event-789",
      "categoryId": "category-101",
      "userId": "user-123",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,..."
    },
    {...}
  ],
  "totalElements": 25
}
```

#### Step 10: View Event Analytics
```
GET /api/v1/analytics/organization/{orgId}
Headers: Authorization: Bearer {accessToken}
Query: ?eventId=event-789
Response: 200 OK
{
  "eventMetrics": {
    "totalTicketsSold": 25,
    "totalRevenue": 12500.00,
    "averageTicketPrice": 500.00,
    "capacityUsed": 5,
    "checkedIn": 20,
    "attendanceRate": 80.0
  }
}
```

---

### FLOW 4: GUEST USER - BROWSE & BUY TICKETS

**Service**: Event Service (8083), Order Service (8084), Ticket Service (8085), Payment Service (8086), Notification Service (8087)  
**Duration**: ~10 endpoints  
**Roles**: Guest/Registered User

#### Step 1: User Login
```
POST /api/v1/users/login
Body: {
  "email": "john@example.com",
  "password": "SecurePass123!"
}
Response: 200 OK
{
  "accessToken": "...",
  "roles": ["ROLE_GUEST"],
  "userId": "user-123"
}
```

#### Step 2: Browse Events (Search)
```
GET /api/v1/events
Headers: Authorization: Bearer {accessToken}
Query: ?search=tech&city=ny&startDate=2026-06-01&page=0&size=20&sort=startDate
Response: 200 OK
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "location": "Convention Center, NY",
      "startDate": "2026-06-15T09:00:00Z",
      "capacity": 500,
      "ticketsSold": 25,
      "categories": [
        {
          "id": "category-101",
          "name": "VIP Pass",
          "price": 500.00,
          "available": 25
        },
        {
          "id": "category-102",
          "name": "Standard Pass",
          "price": 100.00,
          "available": 275
        }
      ]
    },
    {...}
  ],
  "totalElements": 15
}
```

#### Step 3: View Event Details
```
GET /api/v1/events/{eventId}
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "id": "event-789",
  "title": "Tech Conference 2026",
  "description": "Annual tech conference",
  "location": "Convention Center, NY",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-15T17:00:00Z",
  "capacity": 500,
  "ticketsSold": 25,
  "categories": [...],
  "organizerInfo": {
    "name": "Event Company XYZ",
    "image": "..."
  }
}
```

#### Step 4: Create Order (Add to Cart)
```
POST /api/v1/orders
Headers: Authorization: Bearer {accessToken}
Body: {
  "userId": "user-123",
  "eventId": "event-789",
  "items": [
    {
      "categoryId": "category-101",
      "quantity": 1,
      "price": 500.00
    },
    {
      "categoryId": "category-102",
      "quantity": 2,
      "price": 100.00
    }
  ],
  "promoCode": "EARLYBIRD20"
}
Response: 201 Created
{
  "id": "order-1001",
  "userId": "user-123",
  "status": "PENDING",
  "items": [
    {"categoryId": "category-101", "quantity": 1, "price": 500.00},
    {"categoryId": "category-102", "quantity": 2, "discountedPrice": 80.00}
  ],
  "subtotal": 660.00,
  "discount": 40.00,
  "total": 620.00,
  "createdAt": "2026-02-25T12:00:00Z"
}
```

#### Step 5: Process Payment
```
POST /api/v1/payments
Headers: Authorization: Bearer {accessToken}
Body: {
  "orderId": "order-1001",
  "amount": 620.00,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "cvv": "123",
    "holderName": "John Doe"
  }
}
Response: 200 OK
{
  "id": "payment-5001",
  "orderId": "order-1001",
  "amount": 620.00,
  "status": "COMPLETED",
  "transactionId": "txn-abc123",
  "paidAt": "2026-02-25T12:05:00Z"
}
```

#### Step 6: Order Confirmation (Auto-triggered by system)
```
GET /api/v1/orders/{orderId}
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "id": "order-1001",
  "status": "COMPLETED",
  "payment": {
    "status": "COMPLETED",
    "amount": 620.00
  },
  "tickets": [
    {
      "id": "ticket-1001",
      "categoryName": "VIP Pass",
      "qrCode": "data:image/png;base64,...",
      "downloadUrl": "/api/v1/tickets/1001/download"
    },
    {...}
  ]
}
```

#### Step 7: Get User Tickets
```
GET /api/v1/tickets/user/{userId}
Headers: Authorization: Bearer {accessToken}
Query: ?status=ACTIVE
Response: 200 OK
{
  "content": [
    {
      "id": "ticket-1001",
      "eventId": "event-789",
      "eventTitle": "Tech Conference 2026",
      "categoryName": "VIP Pass",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,...",
      "eventStartDate": "2026-06-15T09:00:00Z"
    },
    {...}
  ],
  "totalElements": 3
}
```

#### Step 8: Download Ticket QR Code
```
GET /api/v1/tickets/{ticketId}/qr-code
Headers: Authorization: Bearer {accessToken}
Response: 200 OK (PNG Image)
[Binary image data]
```

#### Step 9: Transfer Ticket
```
POST /api/v1/ticket-transfers
Headers: Authorization: Bearer {accessToken}
Body: {
  "ticketId": "ticket-1001",
  "toUserEmail": "friend@example.com",
  "message": "Enjoy the conference!"
}
Response: 200 OK
{
  "id": "transfer-3001",
  "ticketId": "ticket-1001",
  "fromUser": "user-123",
  "toUserEmail": "friend@example.com",
  "status": "PENDING",
  "expiresAt": "2026-02-27T12:05:00Z"
}
```

#### Step 10: View Order History
```
GET /api/v1/orders/user/{userId}
Headers: Authorization: Bearer {accessToken}
Query: ?page=0&size=20&sort=createdAt,desc
Response: 200 OK
{
  "content": [
    {
      "id": "order-1001",
      "eventTitle": "Tech Conference 2026",
      "totalAmount": 620.00,
      "status": "COMPLETED",
      "createdAt": "2026-02-25T12:00:00Z"
    },
    {...}
  ],
  "totalElements": 5
}
```

---

### FLOW 5: SCANNER - CHECK-IN ATTENDEES

**Service**: Ticket Service (8085), Event Service (8083)  
**Duration**: ~5 endpoints  
**Roles**: Scanner/Operator

#### Step 1: Scanner Login
```
POST /api/v1/users/login
Body: {
  "email": "scanner@eventxyz.com",
  "password": "ScannerPass123!"
}
Response: 200 OK
{
  "accessToken": "...",
  "roles": ["ROLE_SCANNER"],
  "organizationId": "org-456"
}
```

#### Step 2: Get Scanner's Events
```
GET /api/v1/events
Headers: Authorization: Bearer {accessToken}
Query: ?organizationId=org-456&status=PUBLISHED
Response: 200 OK
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "startDate": "2026-06-15T09:00:00Z",
      "capacity": 500,
      "ticketsSold": 25
    },
    {...}
  ]
}
```

#### Step 3: Validate QR Code
```
POST /api/v1/tickets/validate-qr
Headers: Authorization: Bearer {accessToken}
Body: {
  "qrCode": "TICKET-1001-QR-CODE-STRING",
  "eventId": "event-789"
}
Response: 200 OK
{
  "valid": true,
  "ticket": {
    "id": "ticket-1001",
    "eventId": "event-789",
    "categoryName": "VIP Pass",
    "userId": "user-123",
    "userName": "John Doe",
    "status": "ACTIVE"
  }
}
```

#### Step 4: Check-in Ticket
```
POST /api/v1/tickets/{ticketId}/check-in
Headers: Authorization: Bearer {accessToken}
Body: {
  "eventId": "event-789",
  "checkInTime": "2026-06-15T09:15:00Z"
}
Response: 200 OK
{
  "id": "ticket-1001",
  "status": "CHECKED_IN",
  "checkedInAt": "2026-06-15T09:15:00Z",
  "userName": "John Doe",
  "message": "Welcome John Doe!"
}
```

#### Step 5: View Event Attendance Report
```
GET /api/v1/events/{eventId}/attendance
Headers: Authorization: Bearer {accessToken}
Response: 200 OK
{
  "eventId": "event-789",
  "eventTitle": "Tech Conference 2026",
  "totalTicketsSold": 25,
  "totalCheckedIn": 20,
  "attendanceRate": 80.0,
  "checkedInTickets": [
    {
      "id": "ticket-1001",
      "userName": "John Doe",
      "categoryName": "VIP Pass",
      "checkedInAt": "2026-06-15T09:15:00Z"
    },
    {...}
  ]
}
```

---

## 📊 Endpoint Summary by Service

### Service Endpoints Count
| Service | Total Endpoints | Status |
|---------|---|---|
| User Service (8081) | 8 | ✅ |
| Organization Service (8082) | 7 | ✅ |
| Event Service (8083) | 8 | ✅ |
| Order Service (8084) | 5 | ✅ |
| Ticket Service (8085) | 7 | ✅ |
| Payment Service (8086) | 4 | ✅ |
| Notification Service (8087) | 2 | ✅ |
| Analytics Service (8088) | 4 | ✅ |
| **TOTAL** | **45+** | **✅** |

---

## 🔄 Data Flow Between Services

```
User Registration Flow:
User Service → (send email) → Notification Service
                           → (store user) → PostgreSQL
                           → (update cache) → Redis

Event Purchase Flow:
Guest User (Event Service) → Order Service → Payment Service
                          → (create order) → PostgreSQL
                          → (send confirmation) → Notification Service
                          → (update analytics) → Analytics Service
                          → (publish event) → Kafka

Check-in Flow:
Scanner → Ticket Service → (validate QR) → PostgreSQL
                        → (update status) → Redis
                        → (publish event) → Kafka
                        → (update analytics) → Analytics Service
```

---

## ✅ Testing Checklist

- [ ] All endpoints return correct HTTP status codes
- [ ] All endpoints validate authorization
- [ ] Request validation works correctly
- [ ] Response payloads match examples
- [ ] Error messages are clear and helpful
- [ ] Token refresh works correctly
- [ ] Database transactions are atomic
- [ ] Cache is properly invalidated
- [ ] Email notifications are sent
- [ ] Kafka events are published
- [ ] Analytics are recorded
- [ ] Performance is acceptable (<500ms per endpoint)

---

**Last Updated**: February 2026
**Total Endpoints**: 45+
**Status**: Production Ready ✅
