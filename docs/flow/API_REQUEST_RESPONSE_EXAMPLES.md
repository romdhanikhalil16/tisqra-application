# 📝 API Request/Response Examples

## Complete JSON Examples for All Endpoints

This document provides real-world request and response examples for testing all endpoints in Postman.

---

## 🔐 Authentication Endpoints

### 1. User Registration

**Request:**
```
POST http://localhost:8080/api/v1/users/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+1234567890"
}
```

**Success Response (201 Created):**
```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "emailVerified": false,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z",
  "updatedAt": "2026-02-25T10:00:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "path": "/api/v1/users/register"
}
```

---

### 2. Email Verification

**Request:**
```
POST http://localhost:8080/api/v1/users/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "verificationCode": "123456"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Email verified successfully",
  "emailVerified": true,
  "userId": "user-123"
}
```

---

### 3. User Login

**Request:**
```
POST http://localhost:8080/api/v1/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "userId": "user-123",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-xyz123",
  "expiresIn": 1800,
  "roles": ["ROLE_GUEST"],
  "organizationId": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/users/login"
}
```

---

## 🏢 Organization Endpoints

### 4. Create Organization (SuperAdmin Only)

**Request:**
```
POST http://localhost:8080/api/v1/organizations
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "name": "Event Company XYZ",
  "description": "Professional event management company",
  "email": "admin@eventxyz.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "website": "https://eventxyz.com"
}
```

**Success Response (201 Created):**
```json
{
  "id": "org-456",
  "name": "Event Company XYZ",
  "description": "Professional event management company",
  "email": "admin@eventxyz.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "website": "https://eventxyz.com",
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z",
  "updatedAt": "2026-02-25T10:00:00Z"
}
```

---

### 5. Get All Organizations

**Request:**
```
GET http://localhost:8080/api/v1/organizations?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "org-456",
      "name": "Event Company XYZ",
      "email": "admin@eventxyz.com",
      "city": "New York",
      "country": "USA",
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:00:00Z"
    },
    {
      "id": "org-789",
      "name": "Tech Events Inc",
      "email": "admin@techevents.com",
      "city": "San Francisco",
      "country": "USA",
      "status": "ACTIVE",
      "createdAt": "2026-02-24T10:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

### 6. Get Organization Details

**Request:**
```
GET http://localhost:8080/api/v1/organizations/org-456
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "id": "org-456",
  "name": "Event Company XYZ",
  "description": "Professional event management company",
  "email": "admin@eventxyz.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "website": "https://eventxyz.com",
  "status": "ACTIVE",
  "subscriptionPlan": "PREMIUM",
  "subscriptionExpiresAt": "2026-12-31T23:59:59Z",
  "stats": {
    "eventsCreated": 12,
    "ticketsSold": 450,
    "totalRevenue": 15000.00,
    "activeEvents": 5
  },
  "createdAt": "2026-02-25T10:00:00Z",
  "updatedAt": "2026-02-25T10:00:00Z"
}
```

---

## 🎪 Event Endpoints

### 7. Create Event (Admin Org Only)

**Request:**
```
POST http://localhost:8080/api/v1/events
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "title": "Tech Conference 2026",
  "description": "Annual technology conference featuring industry leaders",
  "organizationId": "org-456",
  "location": "Convention Center, New York",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-15T17:00:00Z",
  "capacity": 500,
  "category": "CONFERENCE",
  "image": "https://example.com/tech-conference.jpg",
  "status": "DRAFT"
}
```

**Success Response (201 Created):**
```json
{
  "id": "event-789",
  "title": "Tech Conference 2026",
  "description": "Annual technology conference featuring industry leaders",
  "organizationId": "org-456",
  "location": "Convention Center, New York",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-15T17:00:00Z",
  "capacity": 500,
  "category": "CONFERENCE",
  "image": "https://example.com/tech-conference.jpg",
  "status": "DRAFT",
  "ticketsSold": 0,
  "revenue": 0.00,
  "createdAt": "2026-02-25T10:00:00Z",
  "updatedAt": "2026-02-25T10:00:00Z"
}
```

---

### 8. Search/List Events

**Request:**
```
GET http://localhost:8080/api/v1/events?search=tech&city=New%20York&page=0&size=20&sort=startDate,asc
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "description": "Annual technology conference...",
      "location": "Convention Center, New York",
      "startDate": "2026-06-15T09:00:00Z",
      "endDate": "2026-06-15T17:00:00Z",
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
      ],
      "organizerInfo": {
        "id": "org-456",
        "name": "Event Company XYZ"
      },
      "createdAt": "2026-02-25T10:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 9. Get Event Details

**Request:**
```
GET http://localhost:8080/api/v1/events/event-789
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "id": "event-789",
  "title": "Tech Conference 2026",
  "description": "Annual technology conference...",
  "location": "Convention Center, New York",
  "address": "123 Convention Center Drive",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "startDate": "2026-06-15T09:00:00Z",
  "endDate": "2026-06-15T17:00:00Z",
  "capacity": 500,
  "ticketsSold": 25,
  "revenue": 12500.00,
  "image": "https://example.com/tech-conference.jpg",
  "status": "PUBLISHED",
  "categories": [
    {
      "id": "category-101",
      "name": "VIP Pass",
      "description": "Premium access with benefits",
      "quantity": 50,
      "available": 25,
      "price": 500.00,
      "benefits": ["Priority Seating", "Lunch Included", "Meet & Greet"]
    },
    {
      "id": "category-102",
      "name": "Standard Pass",
      "description": "Regular access",
      "quantity": 300,
      "available": 275,
      "price": 100.00,
      "benefits": []
    }
  ],
  "organizerInfo": {
    "id": "org-456",
    "name": "Event Company XYZ",
    "email": "admin@eventxyz.com",
    "image": "https://example.com/org-logo.jpg"
  },
  "createdAt": "2026-02-25T10:00:00Z",
  "updatedAt": "2026-02-25T10:00:00Z"
}
```

---

## 🎟️ Ticket Category Endpoints

### 10. Create Ticket Category

**Request:**
```
POST http://localhost:8080/api/v1/ticket-categories
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "eventId": "event-789",
  "name": "VIP Pass",
  "description": "Premium access with exclusive benefits",
  "quantity": 50,
  "price": 500.00,
  "benefits": [
    "Priority Seating",
    "Lunch Included",
    "Meet & Greet with Speakers"
  ]
}
```

**Success Response (201 Created):**
```json
{
  "id": "category-101",
  "eventId": "event-789",
  "name": "VIP Pass",
  "description": "Premium access with exclusive benefits",
  "quantity": 50,
  "available": 50,
  "sold": 0,
  "price": 500.00,
  "benefits": ["Priority Seating", "Lunch Included", "Meet & Greet with Speakers"],
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

## 💰 Order Endpoints

### 11. Create Order

**Request:**
```
POST http://localhost:8080/api/v1/orders
Authorization: Bearer {access_token}
Content-Type: application/json

{
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
  "promoCode": "EARLYBIRD20",
  "notes": "Please hold seats near front"
}
```

**Success Response (201 Created):**
```json
{
  "id": "order-1001",
  "userId": "user-123",
  "eventId": "event-789",
  "status": "PENDING",
  "items": [
    {
      "categoryId": "category-101",
      "categoryName": "VIP Pass",
      "quantity": 1,
      "originalPrice": 500.00,
      "discountAmount": 0.00,
      "finalPrice": 500.00
    },
    {
      "categoryId": "category-102",
      "categoryName": "Standard Pass",
      "quantity": 2,
      "originalPrice": 100.00,
      "discountAmount": 20.00,
      "finalPrice": 160.00
    }
  ],
  "subtotal": 660.00,
  "discountAmount": 40.00,
  "promoCode": "EARLYBIRD20",
  "totalAmount": 620.00,
  "notes": "Please hold seats near front",
  "createdAt": "2026-02-25T12:00:00Z"
}
```

---

### 12. Get Order Details

**Request:**
```
GET http://localhost:8080/api/v1/orders/order-1001
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "id": "order-1001",
  "userId": "user-123",
  "eventId": "event-789",
  "eventTitle": "Tech Conference 2026",
  "status": "COMPLETED",
  "items": [
    {
      "id": "order-item-1",
      "categoryName": "VIP Pass",
      "quantity": 1,
      "price": 500.00
    },
    {
      "id": "order-item-2",
      "categoryName": "Standard Pass",
      "quantity": 2,
      "price": 160.00
    }
  ],
  "subtotal": 660.00,
  "discountAmount": 40.00,
  "totalAmount": 620.00,
  "payment": {
    "id": "payment-5001",
    "status": "COMPLETED",
    "amount": 620.00,
    "method": "CREDIT_CARD",
    "transactionId": "txn-abc123",
    "paidAt": "2026-02-25T12:05:00Z"
  },
  "tickets": [
    {
      "id": "ticket-1001",
      "categoryName": "VIP Pass",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,iVBORw0KGg..."
    },
    {
      "id": "ticket-1002",
      "categoryName": "Standard Pass",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,iVBORw0KGg..."
    }
  ],
  "createdAt": "2026-02-25T12:00:00Z"
}
```

---

## 💳 Payment Endpoints

### 13. Process Payment

**Request:**
```
POST http://localhost:8080/api/v1/payments
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "orderId": "order-1001",
  "amount": 620.00,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "cvv": "123",
    "holderName": "John Doe"
  },
  "billingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

**Success Response (200 OK):**
```json
{
  "id": "payment-5001",
  "orderId": "order-1001",
  "amount": 620.00,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "txn-abc123xyz456",
  "last4Digits": "1111",
  "paidAt": "2026-02-25T12:05:00Z",
  "receiptUrl": "https://example.com/receipts/payment-5001.pdf"
}
```

**Error Response (402 Payment Required):**
```json
{
  "timestamp": "2026-02-25T12:05:00Z",
  "status": 402,
  "error": "Payment Failed",
  "message": "Card declined. Please use another card.",
  "path": "/api/v1/payments"
}
```

---

## 🎟️ Ticket Endpoints

### 14. Get User Tickets

**Request:**
```
GET http://localhost:8080/api/v1/tickets/user/user-123?status=ACTIVE&page=0&size=20
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "ticket-1001",
      "eventId": "event-789",
      "eventTitle": "Tech Conference 2026",
      "eventDate": "2026-06-15T09:00:00Z",
      "categoryName": "VIP Pass",
      "categoryId": "category-101",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
      "serialNumber": "TICKET-1001-12345",
      "isPdf": true,
      "pdfUrl": "https://example.com/tickets/ticket-1001.pdf",
      "createdAt": "2026-02-25T12:00:00Z"
    },
    {
      "id": "ticket-1002",
      "eventId": "event-789",
      "eventTitle": "Tech Conference 2026",
      "eventDate": "2026-06-15T09:00:00Z",
      "categoryName": "Standard Pass",
      "categoryId": "category-102",
      "status": "ACTIVE",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
      "serialNumber": "TICKET-1002-12346",
      "isPdf": true,
      "pdfUrl": "https://example.com/tickets/ticket-1002.pdf",
      "createdAt": "2026-02-25T12:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### 15. Validate QR Code (Scanner)

**Request:**
```
POST http://localhost:8080/api/v1/tickets/validate-qr
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "qrCode": "TICKET-1001-QR-CODE-STRING",
  "eventId": "event-789"
}
```

**Success Response (200 OK):**
```json
{
  "valid": true,
  "ticket": {
    "id": "ticket-1001",
    "eventId": "event-789",
    "eventTitle": "Tech Conference 2026",
    "categoryName": "VIP Pass",
    "serialNumber": "TICKET-1001-12345",
    "status": "ACTIVE",
    "userId": "user-123",
    "userName": "John Doe",
    "userEmail": "john@example.com"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2026-02-25T12:05:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Invalid QR code",
  "path": "/api/v1/tickets/validate-qr"
}
```

---

### 16. Check-in Ticket (Scanner)

**Request:**
```
POST http://localhost:8080/api/v1/tickets/ticket-1001/check-in
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "eventId": "event-789",
  "checkInTime": "2026-06-15T09:15:00Z",
  "scannedBy": "scanner-1"
}
```

**Success Response (200 OK):**
```json
{
  "id": "ticket-1001",
  "eventId": "event-789",
  "status": "CHECKED_IN",
  "checkedInAt": "2026-06-15T09:15:00Z",
  "userName": "John Doe",
  "categoryName": "VIP Pass",
  "message": "✅ Welcome John Doe! Your ticket has been checked in.",
  "scannedBy": "scanner-1"
}
```

---

### 17. Transfer Ticket

**Request:**
```
POST http://localhost:8080/api/v1/ticket-transfers
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "ticketId": "ticket-1001",
  "toUserEmail": "friend@example.com",
  "message": "Enjoy the tech conference!"
}
```

**Success Response (201 Created):**
```json
{
  "id": "transfer-3001",
  "ticketId": "ticket-1001",
  "fromUserId": "user-123",
  "fromUserEmail": "john@example.com",
  "toUserEmail": "friend@example.com",
  "status": "PENDING",
  "message": "Enjoy the tech conference!",
  "transferToken": "transfer-token-xyz123",
  "expiresAt": "2026-02-27T12:05:00Z",
  "createdAt": "2026-02-25T12:05:00Z"
}
```

---

## 📊 Analytics Endpoints

### 18. Get Dashboard Analytics (SuperAdmin)

**Request:**
```
GET http://localhost:8080/api/v1/analytics/dashboard
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "systemMetrics": {
    "totalOrganizations": 45,
    "totalUsers": 5000,
    "totalRevenue": 125000.00,
    "activeEvents": 89,
    "ticketsSold": 12000,
    "averageTicketPrice": 10.42
  },
  "revenueByMonth": [
    {
      "month": "January 2026",
      "revenue": 15000.00
    },
    {
      "month": "February 2026",
      "revenue": 18500.00
    }
  ],
  "topEvents": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "ticketsSold": 450,
      "revenue": 15000.00
    }
  ]
}
```

---

### 19. Get Organization Analytics (Admin)

**Request:**
```
GET http://localhost:8080/api/v1/analytics/organization/org-456?eventId=event-789
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "organizationId": "org-456",
  "organizationName": "Event Company XYZ",
  "eventMetrics": {
    "totalTicketsSold": 25,
    "totalRevenue": 12500.00,
    "averageTicketPrice": 500.00,
    "capacityUsed": 5,
    "checkedIn": 20,
    "attendanceRate": 80.0
  },
  "ticketsByCategory": [
    {
      "categoryName": "VIP Pass",
      "sold": 25,
      "revenue": 12500.00
    },
    {
      "categoryName": "Standard Pass",
      "sold": 0,
      "revenue": 0.00
    }
  ]
}
```

---

## ❌ Common Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input: field 'email' is required",
  "path": "/api/v1/users/register"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/v1/users/profile"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/v1/organizations/org-456"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Event with id 'event-999' not found",
  "path": "/api/v1/events/event-999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists",
  "path": "/api/v1/users/register"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-02-25T10:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/v1/events"
}
```

---

**Last Updated**: February 2026
**Version**: 1.0.0
**Status**: Production Ready ✅
