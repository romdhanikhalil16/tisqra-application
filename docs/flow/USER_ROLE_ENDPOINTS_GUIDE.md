# User Role Endpoints Guide

## Overview
This guide organizes all API endpoints by user role with their required permissions and expected responses.

## Role Definitions

| Role | Description | Permissions |
|------|-------------|-------------|
| **SUPERADMIN** | System administrator | Full access to all resources |
| **ADMIN_ORG** | Organization administrator | Full access to organization resources |
| **GUEST** | Regular user/ticket buyer | Limited access to public resources |
| **SCANNER** | Event scanner/entry staff | Access to scanning endpoints only |

---

## SUPERADMIN Endpoints

### Authentication
```
POST /auth/login
Headers: None
Body: { email, password }
Response: 200 { accessToken, refreshToken, userId, role }
```

### User Management
```
GET /users
Headers: Authorization: Bearer {token}
Query: ?page=0&size=10&role=ADMIN_ORG
Response: 200 { content: [users], totalElements, totalPages }

POST /users/admin
Headers: Authorization: Bearer {token}
Body: { email, password, firstName, lastName, role }
Response: 201 { id, email, role, createdAt }

GET /users/{userId}
Headers: Authorization: Bearer {token}
Response: 200 { id, email, firstName, lastName, role, createdAt }

PUT /users/{userId}
Headers: Authorization: Bearer {token}
Body: { firstName, lastName, phoneNumber, status }
Response: 200 { id, email, firstName, lastName, updatedAt }

DELETE /users/{userId}
Headers: Authorization: Bearer {token}
Response: 204 No Content
```

### Organization Management
```
GET /organizations
Headers: Authorization: Bearer {token}
Query: ?page=0&size=10&status=ACTIVE
Response: 200 { content: [organizations], totalElements, totalPages }

GET /organizations/{orgId}
Headers: Authorization: Bearer {token}
Response: 200 { id, name, email, phoneNumber, address, status, createdAt }

POST /organizations/{orgId}/suspend
Headers: Authorization: Bearer {token}
Response: 200 { id, status: "SUSPENDED" }

POST /organizations/{orgId}/activate
Headers: Authorization: Bearer {token}
Response: 200 { id, status: "ACTIVE" }
```

### System Statistics
```
GET /admin/statistics
Headers: Authorization: Bearer {token}
Response: 200 {
  totalUsers: number,
  totalOrganizations: number,
  totalEvents: number,
  totalRevenue: number,
  activeOrganizations: number
}

GET /admin/reports/daily
Headers: Authorization: Bearer {token}
Query: ?date=2024-01-01
Response: 200 { date, newUsers, newOrders, revenue, topEvents: [] }
```

---

## ADMIN_ORG Endpoints

### Authentication
```
POST /auth/register
Body: { email, password, firstName, lastName, role: "ADMIN_ORG" }
Response: 201 { id, email, role, createdAt }

POST /auth/login
Body: { email, password }
Response: 200 { accessToken, refreshToken, userId, organizationId }
```

### Organization Management
```
GET /organizations/me
Headers: Authorization: Bearer {token}
Response: 200 { id, name, email, members: [], events: [] }

PUT /organizations/me
Headers: Authorization: Bearer {token}
Body: { name, description, phoneNumber, address, website }
Response: 200 { id, name, updatedAt }

GET /organizations/me/members
Headers: Authorization: Bearer {token}
Response: 200 { content: [members], totalElements }

POST /organizations/me/members
Headers: Authorization: Bearer {token}
Body: { email, role }
Response: 201 { id, email, role, status }

DELETE /organizations/me/members/{memberId}
Headers: Authorization: Bearer {token}
Response: 204 No Content
```

### Event Management
```
POST /events
Headers: Authorization: Bearer {token}
Body: {
  title, description, startDate, endDate,
  location, capacity, categoryId, organizationId
}
Response: 201 { id, title, organizationId, status: "DRAFT" }

GET /events
Headers: Authorization: Bearer {token}
Query: ?organizationId={orgId}&page=0&size=10
Response: 200 { content: [events], totalElements, totalPages }

GET /events/{eventId}
Headers: Authorization: Bearer {token}
Response: 200 { id, title, description, startDate, endDate, location, capacity }

PUT /events/{eventId}
Headers: Authorization: Bearer {token}
Body: { title, description, startDate, endDate, capacity }
Response: 200 { id, title, updatedAt }

POST /events/{eventId}/publish
Headers: Authorization: Bearer {token}
Response: 200 { id, status: "PUBLISHED" }

DELETE /events/{eventId}
Headers: Authorization: Bearer {token}
Response: 204 No Content
```

### Ticket Management
```
POST /tickets
Headers: Authorization: Bearer {token}
Body: { eventId, ticketType, price, quantity, description }
Response: 201 { id, eventId, ticketType, price }

GET /tickets
Headers: Authorization: Bearer {token}
Query: ?eventId={eventId}&page=0&size=10
Response: 200 { content: [tickets], totalElements }

PUT /tickets/{ticketId}
Headers: Authorization: Bearer {token}
Body: { price, quantity, description }
Response: 200 { id, price, quantity, updatedAt }

DELETE /tickets/{ticketId}
Headers: Authorization: Bearer {token}
Response: 204 No Content
```

### Analytics
```
GET /analytics/events/{eventId}
Headers: Authorization: Bearer {token}
Response: 200 {
  eventId, title, totalTickets, soldTickets,
  totalRevenue, attendanceRate
}

GET /analytics/organizations/me
Headers: Authorization: Bearer {token}
Response: 200 {
  organizationId, totalEvents, totalTickets,
  totalRevenue, topEvents: []
}

GET /analytics/reports/{eventId}
Headers: Authorization: Bearer {token}
Response: 200 { eventId, date, sales, attendance, revenue }
```

---

## GUEST Endpoints

### Authentication
```
POST /auth/register
Body: { email, password, firstName, lastName }
Response: 201 { id, email, firstName, role }

POST /auth/login
Body: { email, password }
Response: 200 { accessToken, refreshToken, userId }

POST /auth/logout
Headers: Authorization: Bearer {token}
Response: 200 { message: "Logged out successfully" }
```

### Browse Events
```
GET /events/public
Query: ?page=0&size=20&category=MUSIC&location=NYC
Response: 200 { content: [events], totalElements, totalPages }

GET /events/{eventId}/public
Response: 200 { id, title, description, startDate, location, capacity, soldTickets }

GET /categories
Response: 200 { content: [categories] }

GET /events/search
Query: ?q=concert&location=NYC
Response: 200 { content: [events] }
```

### Profile Management
```
GET /users/me
Headers: Authorization: Bearer {token}
Response: 200 { id, email, firstName, lastName, phoneNumber, createdAt }

PUT /users/me
Headers: Authorization: Bearer {token}
Body: { firstName, lastName, phoneNumber, avatar }
Response: 200 { id, email, firstName, updatedAt }

PUT /users/me/password
Headers: Authorization: Bearer {token}
Body: { currentPassword, newPassword }
Response: 200 { message: "Password updated" }
```

### Ticket Purchase
```
POST /orders
Headers: Authorization: Bearer {token}
Body: { eventId, tickets: [{ ticketId, quantity }], paymentMethod }
Response: 201 { id, eventId, totalAmount, status: "PENDING" }

GET /orders
Headers: Authorization: Bearer {token}
Query: ?page=0&size=10
Response: 200 { content: [orders], totalElements }

GET /orders/{orderId}
Headers: Authorization: Bearer {token}
Response: 200 { id, eventId, tickets: [], totalAmount, status }

GET /orders/{orderId}/tickets
Headers: Authorization: Bearer {token}
Response: 200 { content: [tickets], totalElements }
```

### Ticket Operations
```
GET /tickets/me
Headers: Authorization: Bearer {token}
Response: 200 { content: [myTickets], totalElements }

GET /tickets/{ticketId}/qr
Headers: Authorization: Bearer {token}
Response: 200 { qrCode: "base64_encoded_image" }

POST /tickets/{ticketId}/transfer
Headers: Authorization: Bearer {token}
Body: { email, quantity }
Response: 201 { id, status: "TRANSFERRED_PENDING" }

GET /ticket-transfers
Headers: Authorization: Bearer {token}
Response: 200 { content: [transfers], totalElements }

POST /ticket-transfers/{transferId}/accept
Headers: Authorization: Bearer {token}
Response: 200 { id, status: "ACCEPTED" }
```

### Payments
```
POST /payments
Headers: Authorization: Bearer {token}
Body: {
  orderId, amount, currency, paymentMethod,
  cardDetails: { cardNumber, expiryMonth, expiryYear, cvv }
}
Response: 201 { id, orderId, status: "PROCESSING" }

GET /payments/{paymentId}
Headers: Authorization: Bearer {token}
Response: 200 { id, orderId, amount, status, createdAt }

POST /payments/{paymentId}/refund
Headers: Authorization: Bearer {token}
Body: { reason }
Response: 200 { id, status: "REFUNDED" }
```

### Notifications
```
GET /notifications
Headers: Authorization: Bearer {token}
Response: 200 { content: [notifications], totalElements }

PUT /notifications/{notificationId}/read
Headers: Authorization: Bearer {token}
Response: 200 { id, read: true }

DELETE /notifications/{notificationId}
Headers: Authorization: Bearer {token}
Response: 204 No Content
```

---

## SCANNER Endpoints

### Authentication
```
POST /auth/login
Body: { email, password }
Response: 200 { accessToken, eventId, organizationId }

POST /auth/logout
Headers: Authorization: Bearer {token}
Response: 200 { message: "Logged out" }
```

### Ticket Scanning
```
POST /tickets/scan
Headers: Authorization: Bearer {token}
Body: { qrCode, eventId }
Response: 200 {
  id, ticketNumber, holderName, eventId,
  status: "VALID", verifiedAt
}

PUT /tickets/{ticketId}/attended
Headers: Authorization: Bearer {token}
Body: { eventId, attendedAt }
Response: 200 { id, status: "ATTENDED" }
```

### Statistics
```
GET /scanner/statistics
Headers: Authorization: Bearer {token}
Query: ?eventId={eventId}
Response: 200 {
  eventId, totalTickets, scannedTickets,
  attendanceRate, lastScanAt
}

GET /scanner/attendance
Headers: Authorization: Bearer {token}
Query: ?eventId={eventId}&page=0&size=50
Response: 200 { content: [attendance], totalElements }
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "path": "/api/endpoint"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Common Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | integer | Page number (0-indexed) |
| `size` | integer | Page size (default: 10) |
| `sort` | string | Sort field, e.g., `createdAt,desc` |
| `search` | string | Search query |
| `status` | string | Filter by status |
| `from` | date | From date (yyyy-MM-dd) |
| `to` | date | To date (yyyy-MM-dd) |

---

## Authentication Headers

All protected endpoints require:
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

---

## Rate Limiting

- **Standard**: 100 requests per minute
- **Premium**: 1000 requests per minute
- **Admin**: Unlimited

Response headers include:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1640995200
```
