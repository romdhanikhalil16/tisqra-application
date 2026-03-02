# 🎯 Tisqra Platform - Flows & Endpoints Master Index

## 📋 Complete Navigation Guide

This is your master guide to all application flows, endpoints, and testing documentation.

---

## 🚀 Quick Start (Choose Your Path)

### 👨‍💼 I'm a SuperAdmin
**What you do**: Manage the entire platform, organizations, admins, and view system analytics
- **Start with**: `SUPERADMIN_FLOW_DETAILED.md`
- **Then read**: `KEYCLOAK_AUTH_SETUP_GUIDE.md`
- **Test with**: Postman collection `Tisqra-Complete-User-Flows`

### 👔 I'm an Organization Admin
**What you do**: Create events, manage tickets, prices, and view organization analytics
- **Start with**: `ADMIN_ORG_FLOW_DETAILED.md`
- **Key endpoints**: Event Service (8083), Order Service (8084), Payment Service (8086)
- **Test with**: Postman collection - Admin Org folder

### 👥 I'm a Guest User
**What you do**: Browse events, buy tickets, transfer tickets, view your orders
- **Start with**: `GUEST_USER_FLOW_DETAILED.md`
- **Key endpoints**: Event Service (8083), Ticket Service (8085), Order Service (8084)
- **Test with**: Postman collection - Guest User folder

### 📱 I'm a Scanner/Operator
**What you do**: Scan QR codes, validate tickets, check-in attendees at events
- **Start with**: `SCANNER_USER_FLOW_DETAILED.md`
- **Key endpoints**: Ticket Service (8085), Event Service (8083)
- **Test with**: Postman collection - Scanner folder

### 🧪 I'm a QA/Tester
**What you do**: Test all flows, verify endpoints, validate error handling
- **Start with**: `COMPLETE_TESTING_FLOW_GUIDE.md`
- **Then**: `POSTMAN_TESTING_WORKFLOW.md`
- **Use**: `API_REQUEST_RESPONSE_EXAMPLES.md` for reference data

### 👨‍💻 I'm a Developer
**What you do**: Implement frontend, backend, or mobile features
- **Start with**: `APPLICATION_FLOWS_WITH_ENDPOINTS.md`
- **Reference**: `API_REQUEST_RESPONSE_EXAMPLES.md`
- **Understand**: `USER_ROLE_ENDPOINTS_GUIDE.md`

---

## 📚 Complete Document Catalog

### 1. Core Flow Documentation

#### `APPLICATION_FLOWS_WITH_ENDPOINTS.md`
- **Purpose**: Complete application architecture and flow
- **Contains**: 
  - System overview and architecture
  - All endpoints in testing order
  - Service dependencies
  - Data flow diagrams
  - Integration points
- **For**: Everyone who needs overview

#### `USER_ROLE_ENDPOINTS_GUIDE.md`
- **Purpose**: Endpoint reference organized by user role
- **Contains**:
  - SuperAdmin endpoints (25+)
  - Admin Org endpoints (35+)
  - Guest User endpoints (20+)
  - Scanner endpoints (10+)
  - Permission levels per endpoint
  - HTTP methods and parameters
- **For**: API reference and permission verification

### 2. User Role Specific Flows

#### `SUPERADMIN_FLOW_DETAILED.md`
- **Endpoints**: 25+
- **Services**: User Service, Organization Service, Analytics Service
- **Flow**:
  ```
  1. Login → Dashboard
  2. Manage Organizations (Create, Read, Update, Delete)
  3. Manage Organization Admins
  4. Manage Subscription Plans
  5. View System Analytics
  6. System Configuration
  7. View Audit Logs
  ```
- **Key Endpoints**:
  - POST /api/v1/users/register (SuperAdmin)
  - GET /api/v1/organizations
  - POST /api/v1/organizations
  - PUT /api/v1/organizations/{id}
  - GET /api/v1/subscriptions
  - GET /api/v1/analytics/dashboard
  - GET /api/v1/users/audit-logs

#### `ADMIN_ORG_FLOW_DETAILED.md`
- **Endpoints**: 35+
- **Services**: Event, Order, Ticket, Payment, Organization
- **Flow**:
  ```
  1. Login → Organization Dashboard
  2. Create & Manage Events
  3. Set Ticket Categories & Pricing
  4. Configure Event Schedules
  5. Create Promo Codes
  6. View Orders
  7. View Sold Tickets
  8. Manage Subscriptions
  9. View Organization Analytics
  10. Manage Organization Profile
  ```
- **Key Endpoints**:
  - POST /api/v1/events (create event)
  - PUT /api/v1/events/{id} (update event)
  - POST /api/v1/ticket-categories (create category)
  - GET /api/v1/orders/organization/{orgId}
  - GET /api/v1/tickets/organization/{orgId}
  - GET /api/v1/analytics/organization/{orgId}
  - POST /api/v1/branding (customize organization)

#### `GUEST_USER_FLOW_DETAILED.md`
- **Endpoints**: 20+
- **Services**: Auth, Event, Order, Ticket, Payment, Notification
- **Flow**:
  ```
  1. Register → Email Verification
  2. Login → Home Dashboard
  3. Browse & Search Events
  4. View Event Details
  5. Add Event to Cart
  6. Checkout
  7. Payment Processing
  8. Receive Confirmation Email
  9. View My Tickets
  10. Download Ticket PDF
  11. Transfer Ticket to Another User
  12. View Order History
  13. Manage Profile
  ```
- **Key Endpoints**:
  - POST /api/v1/users/register
  - POST /api/v1/users/verify-email
  - GET /api/v1/events (search & filter)
  - GET /api/v1/events/{id}
  - POST /api/v1/orders (create order)
  - POST /api/v1/payments (process payment)
  - GET /api/v1/tickets/user/{userId}
  - POST /api/v1/ticket-transfers (transfer ticket)
  - GET /api/v1/orders/user/{userId}

#### `SCANNER_USER_FLOW_DETAILED.md`
- **Endpoints**: 10+
- **Services**: Ticket Service, Event Service
- **Flow**:
  ```
  1. Login
  2. Select Event for Scanning
  3. Open Scanner (Camera)
  4. Scan QR Code on Ticket
  5. Validate Ticket
  6. Confirm Check-in
  7. Generate Attendance Report
  ```
- **Key Endpoints**:
  - GET /api/v1/events (list scanner's events)
  - POST /api/v1/tickets/validate-qr
  - POST /api/v1/tickets/{id}/check-in
  - GET /api/v1/events/{id}/attendance

### 3. Setup & Configuration

#### `KEYCLOAK_AUTH_SETUP_GUIDE.md`
- **Purpose**: OAuth2/JWT authentication setup and configuration
- **Contains**:
  - Keycloak admin console access
  - Creating test users
  - Role mapping
  - Client configuration
  - OAuth2 token generation
  - JWT token structure
  - Token refresh mechanism
  - Setting up roles for each user type
- **Steps**:
  1. Access Keycloak: http://localhost:8180
  2. Create realm "tisqra"
  3. Create clients for mobile and web
  4. Create roles: ROLE_SUPERADMIN, ROLE_ADMIN_ORG, ROLE_GUEST, ROLE_SCANNER
  5. Create test users
  6. Map roles to users
  7. Generate OAuth2 tokens for testing

### 4. API Reference & Examples

#### `API_REQUEST_RESPONSE_EXAMPLES.md`
- **Purpose**: Real JSON examples for all endpoints
- **Contains**:
  - Sample request payloads
  - Real response examples
  - Error responses
  - HTTP status codes
  - Data validation examples
  - All field types explained
  - Sample values for each field type
- **Organized by**:
  - User Service
  - Organization Service
  - Event Service
  - Order Service
  - Ticket Service
  - Payment Service
  - Notification Service
  - Analytics Service

### 5. Testing Guides

#### `POSTMAN_TESTING_WORKFLOW.md`
- **Purpose**: Step-by-step Postman setup and testing
- **Contains**:
  - Download and install Postman
  - Import Postman collection
  - Set up environment variables
  - Configure base URL
  - Set up authentication
  - Running request sequences
  - Validating responses
  - Troubleshooting common issues
- **Steps**:
  1. Import collection from `postman/Tisqra-Complete-User-Flows.postman_collection.json`
  2. Create environment with variables
  3. Set base_url = http://localhost:8080
  4. Get OAuth2 token
  5. Store token in variable
  6. Run requests in order
  7. Validate responses against examples

#### `COMPLETE_TESTING_FLOW_GUIDE.md`
- **Purpose**: End-to-end testing scenarios and validation
- **Contains**:
  - Full flow testing for each user role
  - Edge cases and error handling
  - Performance testing tips
  - Load testing considerations
  - Security testing checklist
  - Data validation examples
  - Real-world scenarios
- **Test Scenarios**:
  1. Happy path: User registers → Buys ticket → Gets confirmation
  2. Error case: Invalid payment → Shows error
  3. Permission case: Guest can't create event
  4. Edge case: User transfers ticket to self
  5. Security case: Invalid token rejected
  6. Validation case: Missing required fields

---

## 🔐 User Roles & Permissions Matrix

| Endpoint | SuperAdmin | Admin Org | Guest | Scanner |
|----------|-----------|----------|-------|---------|
| Create Organization | ✅ | ❌ | ❌ | ❌ |
| Create Event | ❌ | ✅ | ❌ | ❌ |
| Browse Events | ✅ | ✅ | ✅ | ✅ |
| Buy Tickets | ❌ | ❌ | ✅ | ❌ |
| Scan QR Code | ❌ | ❌ | ❌ | ✅ |
| Check-in User | ❌ | ❌ | ❌ | ✅ |
| View Analytics | ✅ | ✅ | ❌ | ❌ |
| Manage Users | ✅ | ❌ | ❌ | ❌ |
| Manage Roles | ✅ | ❌ | ❌ | ❌ |

---

## 🔗 Service Endpoints Overview

### User Service (Port 8081)
```
POST   /api/v1/users/register          - Register new user
POST   /api/v1/users/login             - Login user
POST   /api/v1/users/verify-email      - Verify email
POST   /api/v1/users/refresh-token     - Refresh access token
GET    /api/v1/users/{id}              - Get user profile
PUT    /api/v1/users/{id}              - Update user profile
GET    /api/v1/users/audit-logs        - View audit logs (SuperAdmin)
POST   /api/v1/users/{id}/change-password - Change password
```

### Organization Service (Port 8082)
```
POST   /api/v1/organizations           - Create organization (SuperAdmin)
GET    /api/v1/organizations           - List organizations
GET    /api/v1/organizations/{id}      - Get organization details
PUT    /api/v1/organizations/{id}      - Update organization
DELETE /api/v1/organizations/{id}      - Delete organization (SuperAdmin)
POST   /api/v1/branding                - Set organization branding
PUT    /api/v1/subscriptions/{id}      - Manage subscription
```

### Event Service (Port 8083)
```
POST   /api/v1/events                  - Create event (Admin Org)
GET    /api/v1/events                  - List events (search, filter)
GET    /api/v1/events/{id}             - Get event details
PUT    /api/v1/events/{id}             - Update event (Admin Org)
DELETE /api/v1/events/{id}             - Delete event (Admin Org)
POST   /api/v1/ticket-categories       - Create ticket category
PUT    /api/v1/events/{id}/schedule    - Set event schedule
POST   /api/v1/promo-codes             - Create promo code
```

### Order Service (Port 8084)
```
POST   /api/v1/orders                  - Create order (Guest)
GET    /api/v1/orders/{id}             - Get order details
GET    /api/v1/orders/user/{userId}    - Get user's orders (Guest)
GET    /api/v1/orders/organization/{orgId} - Get org's orders (Admin)
GET    /api/v1/orders/{id}/items       - Get order items
```

### Ticket Service (Port 8085)
```
GET    /api/v1/tickets/user/{userId}   - Get user's tickets
GET    /api/v1/tickets/{id}            - Get ticket details
POST   /api/v1/tickets/{id}/check-in   - Check-in ticket (Scanner)
POST   /api/v1/tickets/validate-qr     - Validate QR code
POST   /api/v1/ticket-transfers        - Transfer ticket (Guest)
GET    /api/v1/tickets/{id}/qr-code    - Download QR code
```

### Payment Service (Port 8086)
```
POST   /api/v1/payments                - Process payment (Guest)
GET    /api/v1/payments/{id}           - Get payment details
POST   /api/v1/refunds                 - Request refund
GET    /api/v1/refunds/{id}            - Get refund details
```

### Notification Service (Port 8087)
```
GET    /api/v1/notifications/user/{userId} - Get user notifications
POST   /api/v1/notifications/{id}/read - Mark as read
```

### Analytics Service (Port 8088)
```
GET    /api/v1/analytics/dashboard     - System analytics (SuperAdmin)
GET    /api/v1/analytics/organization/{orgId} - Org analytics (Admin)
GET    /api/v1/analytics/sales         - Sales reports
GET    /api/v1/analytics/attendance    - Attendance metrics
```

---

## 📊 Testing Workflow Summary

### Step 1: Setup (30 minutes)
- [ ] Install Postman
- [ ] Setup Keycloak users and roles
- [ ] Import Postman collection
- [ ] Configure environment variables

### Step 2: Authentication (15 minutes)
- [ ] Get SuperAdmin token
- [ ] Get Admin Org token
- [ ] Get Guest token
- [ ] Get Scanner token

### Step 3: Test Each Role (2-3 hours)
- [ ] Test SuperAdmin flow (1-2 hours)
- [ ] Test Admin Org flow (1-2 hours)
- [ ] Test Guest User flow (1-2 hours)
- [ ] Test Scanner flow (30 minutes)

### Step 4: Validation (30 minutes)
- [ ] Verify all error cases
- [ ] Check permission enforcement
- [ ] Validate data accuracy
- [ ] Review response times

---

## 🎯 Common Testing Scenarios

### Scenario 1: New User Journey
```
1. User registers via API
2. User receives verification email
3. User verifies email
4. User logs in
5. User browses events
6. User buys ticket
7. User receives confirmation
8. User downloads ticket PDF
```

### Scenario 2: Event Management
```
1. Admin logs in
2. Admin creates event
3. Admin sets ticket categories
4. Admin sets pricing
5. Admin publishes event
6. Users start buying tickets
7. Admin views orders
8. Admin views analytics
```

### Scenario 3: Check-in Process
```
1. Scanner logs in
2. Scanner selects event
3. Scanner opens QR scanner
4. User shows QR code
5. Scanner scans code
6. Ticket validates
7. User checked in
8. Attendance updated
```

---

## 📞 Support & Troubleshooting

### Common Issues

**Q: Token expired, how to refresh?**
A: Use endpoint `POST /api/v1/users/refresh-token` with refresh token

**Q: Getting 403 Permission Denied?**
A: Check user role in Keycloak, verify role mapping correct

**Q: Endpoint returns 404?**
A: Verify service is running on correct port, check API Gateway routing

**Q: Email verification not working?**
A: Check Notification Service is running, review BREVO email settings

**Q: Can't see organizations?**
A: Verify you're logged in as SuperAdmin, check organization list endpoint

---

## 🚀 Next Steps

1. **Choose your user role** → Follow corresponding detailed flow document
2. **Setup Keycloak** → Read `KEYCLOAK_AUTH_SETUP_GUIDE.md`
3. **Setup Postman** → Read `POSTMAN_TESTING_WORKFLOW.md`
4. **Get API examples** → Review `API_REQUEST_RESPONSE_EXAMPLES.md`
5. **Start testing** → Import Postman collection and run flows
6. **Validate responses** → Compare with examples in documentation

---

## 📈 Document Statistics

| Metric | Count |
|--------|-------|
| Total Documents | 12 |
| Total Endpoints | 150+ |
| User Roles | 4 |
| Services | 8 |
| Test Scenarios | 20+ |
| Example Requests | 100+ |
| Example Responses | 100+ |

---

**Last Updated**: February 2026  
**Version**: 1.0.0  
**Status**: Production Ready ✅
