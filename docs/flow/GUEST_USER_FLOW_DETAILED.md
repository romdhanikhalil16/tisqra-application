# 👥 Guest User Complete User Flow

## Guest/Registered User Role - Complete Workflow & Endpoints

This guide covers the complete workflow for Guest users with all 20+ endpoints in testing order.

---

## 📋 Guest User Overview

**Role**: Guest/Registered User  
**Access Level**: Public access (browse, purchase, manage tickets)  
**Responsibilities**: Browse events, purchase tickets, manage orders, transfer tickets  
**Test User**: john@example.com / SecurePass123!  
**First Name**: John  
**Last Name**: Doe  

---

## 📝 Step 1: User Registration

### 1.1 Register New User
```
POST http://localhost:8080/api/v1/users/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+1-555-123-4567"
}
```

### Response (201 Created)
```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1-555-123-4567",
  "emailVerified": false,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

### 1.2 Verify Email
```
POST http://localhost:8080/api/v1/users/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "verificationCode": "123456"  // Received in email
}
```

### Response (200 OK)
```json
{
  "message": "Email verified successfully",
  "emailVerified": true,
  "userId": "user-123"
}
```

---

## 🔐 Step 2: User Login

### Login Request
```
POST http://localhost:8080/api/v1/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

### Expected Response
```json
{
  "userId": "user-123",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-xyz",
  "expiresIn": 1800,
  "roles": ["ROLE_GUEST"]
}
```

**Save**: `accessToken` for all subsequent requests.

---

## 🏠 Step 3: View User Profile

### Get Profile
```
GET http://localhost:8080/api/v1/users/profile
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1-555-123-4567",
  "emailVerified": true,
  "createdAt": "2026-02-25T10:00:00Z",
  "lastLogin": "2026-02-25T10:05:00Z"
}
```

---

## 🔍 Step 4: Browse & Search Events

### 4.1 Get All Events
```
GET http://localhost:8080/api/v1/events?page=0&size=20&sort=startDate,asc
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "location": "Convention Center, NY",
      "startDate": "2026-06-15T09:00:00Z",
      "endDate": "2026-06-15T17:00:00Z",
      "capacity": 500,
      "ticketsSold": 25,
      "image": "https://example.com/tech-conference.jpg",
      "categories": [
        {"id": "category-101", "name": "VIP Pass", "price": 500.00, "available": 25},
        {"id": "category-102", "name": "Standard Pass", "price": 100.00, "available": 275}
      ],
      "organizerInfo": {
        "id": "org-456",
        "name": "Event Company XYZ"
      }
    },
    {...more events}
  ],
  "pageable": {
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0
  }
}
```

---

### 4.2 Search Events by Keywords
```
GET http://localhost:8080/api/v1/events?search=tech&page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "location": "Convention Center, NY",
      "startDate": "2026-06-15T09:00:00Z",
      "categories": [...]
    },
    {
      "id": "event-tech-summit",
      "title": "Tech Summit 2026",
      "location": "San Francisco, CA",
      "startDate": "2026-08-10T09:00:00Z",
      "categories": [...]
    }
  ],
  "totalElements": 12
}
```

---

### 4.3 Filter Events by Location
```
GET http://localhost:8080/api/v1/events?city=New%20York&state=NY&page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "location": "Convention Center, New York",
      "city": "New York",
      "state": "NY",
      "startDate": "2026-06-15T09:00:00Z",
      "categories": [...]
    },
    {...more events in NY}
  ],
  "totalElements": 45
}
```

---

### 4.4 Filter Events by Date Range
```
GET http://localhost:8080/api/v1/events?startDate=2026-06-01&endDate=2026-06-30&page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "startDate": "2026-06-15T09:00:00Z",
      "categories": [...]
    }
  ],
  "totalElements": 5
}
```

---

## 📍 Step 5: View Event Details

### Get Event Details
```
GET http://localhost:8080/api/v1/events/event-789
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "event-789",
  "title": "Tech Conference 2026",
  "description": "Annual technology conference featuring industry leaders",
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
      "price": 100.00
    }
  ],
  "organizerInfo": {
    "id": "org-456",
    "name": "Event Company XYZ",
    "email": "admin@eventxyz.com",
    "image": "https://example.com/org-logo.jpg"
  },
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

## 🛒 Step 6: Create Order (Shopping Cart)

### Create Order with Multiple Items
```
POST http://localhost:8080/api/v1/orders
Authorization: Bearer {accessToken}
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
  "notes": "Please hold seats near the stage"
}
```

### Response (201 Created)
```json
{
  "id": "order-1001",
  "userId": "user-123",
  "eventId": "event-789",
  "eventTitle": "Tech Conference 2026",
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
  "notes": "Please hold seats near the stage",
  "createdAt": "2026-02-25T12:00:00Z"
}
```

**Save**: `orderId` = `order-1001`

---

## 💳 Step 7: Payment Processing

### Process Payment
```
POST http://localhost:8080/api/v1/payments
Authorization: Bearer {accessToken}
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
    "street": "789 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

### Response (200 OK)
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

---

## 📧 Step 8: Order Confirmation (Auto-Triggered)

### Get Updated Order Status
```
GET http://localhost:8080/api/v1/orders/order-1001
Authorization: Bearer {accessToken}
```

### Response
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
    "transactionId": "txn-abc123xyz456",
    "paidAt": "2026-02-25T12:05:00Z"
  },
  "tickets": [
    {
      "id": "ticket-1001",
      "categoryName": "VIP Pass",
      "status": "ACTIVE",
      "serialNumber": "TICKET-1001-12345",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
      "pdfUrl": "https://example.com/tickets/ticket-1001.pdf"
    },
    {
      "id": "ticket-1002",
      "categoryName": "Standard Pass",
      "status": "ACTIVE",
      "serialNumber": "TICKET-1002-12346",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
      "pdfUrl": "https://example.com/tickets/ticket-1002.pdf"
    }
  ],
  "createdAt": "2026-02-25T12:00:00Z"
}
```

---

## 🎟️ Step 9: Manage Your Tickets

### 9.1 View Your Tickets
```
GET http://localhost:8080/api/v1/tickets/user/user-123?status=ACTIVE&page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
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
      "serialNumber": "TICKET-1001-12345",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
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
      "serialNumber": "TICKET-1002-12346",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
      "isPdf": true,
      "pdfUrl": "https://example.com/tickets/ticket-1002.pdf",
      "createdAt": "2026-02-25T12:00:00Z"
    }
  ],
  "totalElements": 2
}
```

---

### 9.2 Download Ticket QR Code
```
GET http://localhost:8080/api/v1/tickets/ticket-1001/qr-code
Authorization: Bearer {accessToken}
```

### Response
```
Binary PNG image data
Content-Type: image/png
```

---

### 9.3 Download Ticket PDF
```
GET http://localhost:8080/api/v1/tickets/ticket-1001/pdf
Authorization: Bearer {accessToken}
```

### Response
```
Binary PDF file data
Content-Type: application/pdf
```

---

## 🔄 Step 10: Transfer Ticket

### Transfer Ticket to Friend
```
POST http://localhost:8080/api/v1/ticket-transfers
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "ticketId": "ticket-1002",
  "toUserEmail": "friend@example.com",
  "message": "Enjoy the tech conference! See you there!"
}
```

### Response (201 Created)
```json
{
  "id": "transfer-3001",
  "ticketId": "ticket-1002",
  "fromUserId": "user-123",
  "fromUserEmail": "john@example.com",
  "toUserEmail": "friend@example.com",
  "status": "PENDING",
  "message": "Enjoy the tech conference! See you there!",
  "transferToken": "transfer-token-xyz123",
  "expiresAt": "2026-02-27T12:05:00Z",
  "createdAt": "2026-02-25T12:05:00Z"
}
```

---

### View Transfer Status
```
GET http://localhost:8080/api/v1/ticket-transfers/transfer-3001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "transfer-3001",
  "ticketId": "ticket-1002",
  "fromUserEmail": "john@example.com",
  "toUserEmail": "friend@example.com",
  "status": "ACCEPTED",
  "acceptedAt": "2026-02-25T12:10:00Z"
}
```

---

## 📋 Step 11: View Order History

### Get All Your Orders
```
GET http://localhost:8080/api/v1/orders/user/user-123?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "order-1001",
      "eventTitle": "Tech Conference 2026",
      "eventDate": "2026-06-15T09:00:00Z",
      "totalAmount": 620.00,
      "itemCount": 3,
      "status": "COMPLETED",
      "paymentStatus": "PAID",
      "createdAt": "2026-02-25T12:00:00Z"
    },
    {
      "id": "order-1002",
      "eventTitle": "Summer Music Festival 2026",
      "eventDate": "2026-07-20T14:00:00Z",
      "totalAmount": 350.00,
      "itemCount": 2,
      "status": "PENDING",
      "paymentStatus": "PENDING",
      "createdAt": "2026-02-24T15:00:00Z"
    }
  ],
  "totalElements": 5
}
```

---

## 👤 Step 12: Manage Profile

### 12.1 Update Profile
```
PUT http://localhost:8080/api/v1/users/user-123
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1-555-987-6543"
}
```

### Response
```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1-555-987-6543",
  "updatedAt": "2026-02-25T12:10:00Z"
}
```

---

### 12.2 Change Password
```
POST http://localhost:8080/api/v1/users/user-123/change-password
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

### Response
```json
{
  "message": "Password changed successfully"
}
```

---

## 📬 Step 13: Manage Notifications

### View Notifications
```
GET http://localhost:8080/api/v1/notifications/user/user-123?page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "notif-1",
      "type": "ORDER_CONFIRMATION",
      "title": "Order Confirmed",
      "message": "Your order for Tech Conference 2026 has been confirmed",
      "read": false,
      "createdAt": "2026-02-25T12:05:00Z"
    },
    {
      "id": "notif-2",
      "type": "TICKET_TRANSFER",
      "title": "Ticket Transfer Request",
      "message": "friend@example.com accepted your ticket transfer",
      "read": false,
      "createdAt": "2026-02-25T12:10:00Z"
    }
  ],
  "unreadCount": 2
}
```

---

### Mark Notification as Read
```
PUT http://localhost:8080/api/v1/notifications/notif-1/read
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "notif-1",
  "read": true,
  "readAt": "2026-02-25T12:15:00Z"
}
```

---

## ✅ Guest User Testing Checklist

- [ ] Can register account
- [ ] Can verify email
- [ ] Can login
- [ ] Can view profile
- [ ] Can browse all events
- [ ] Can search events by keyword
- [ ] Can filter by location
- [ ] Can filter by date
- [ ] Can view event details
- [ ] Can create order with multiple items
- [ ] Can apply promo code
- [ ] Can process payment
- [ ] Order status updated after payment
- [ ] Can view purchased tickets
- [ ] Can download ticket QR code
- [ ] Can download ticket PDF
- [ ] Can transfer ticket to friend
- [ ] Can view order history
- [ ] Can update profile
- [ ] Can change password
- [ ] Can view notifications
- [ ] Can mark notification as read
- [ ] Token refresh works

---

**Total Guest User Endpoints**: 20+  
**Total Estimated Test Time**: 1-2 hours  
**Status**: Production Ready ✅
