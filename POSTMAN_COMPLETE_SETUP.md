# 🚀 Tisqra Platform - Complete Postman Setup Guide

## 📥 Step 1: Import Collection into Postman

### Download & Import
1. Open **Postman** application
2. Click the **Import** button (top-left corner)
3. Select **File** tab
4. Browse to: `postman/Tisqra-Platform.postman_collection.json`
5. Click **Import**

✅ Your collection is now loaded with 150+ ready-to-test endpoints

---

## 🔐 Step 2: Login to Get Access Token

### SuperAdmin Login (Recommended for Testing)

1. In Postman, expand the collection
2. Navigate to: **Auth** → **Login - SuperAdmin**
3. Click **Send**

**Request Details:**
```
POST http://localhost:8080/auth/realms/event-ticketing/protocol/openid-connect/token

Body (form-data):
- grant_type: password
- client_id: event-ticketing-client
- username: admin
- password: admin123
- scope: openid profile email
```

### Response (you'll get):
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "refresh_token": "..."
}
```

✅ Token is automatically stored and used in all requests

---

## 👥 Test User Accounts

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| **SUPER_ADMIN** | admin | admin123 | Full system access |
| **ADMIN_ORG** | org_admin | admin123 | Organization management |
| **SCANNER** | scanner | admin123 | Ticket validation only |
| **GUEST** | guest | guest123 | Browse & purchase tickets |

---

## 📋 API Endpoints by Service

### 🔐 Authentication (Keycloak - Port 8180)
- `POST /auth/realms/event-ticketing/protocol/openid-connect/token` - Get Token
- `POST /auth/realms/event-ticketing/protocol/openid-connect/logout` - Logout
- `POST /auth/realms/event-ticketing/protocol/openid-connect/token` - Refresh Token

### 👤 User Service (Port 8081)
- `POST /api/v1/users` - Create User
- `GET /api/v1/users/{id}` - Get User Details
- `PUT /api/v1/users/{id}` - Update User Profile
- `GET /api/v1/users` - List All Users
- `DELETE /api/v1/users/{id}` - Delete User
- `GET /api/v1/users/{id}/audit-logs` - Get Audit Logs

### 🏢 Organization Service (Port 8082)
- `POST /api/v1/organizations` - Create Organization
- `GET /api/v1/organizations/{id}` - Get Organization
- `PUT /api/v1/organizations/{id}` - Update Organization
- `GET /api/v1/organizations` - List Organizations
- `POST /api/v1/organizations/{id}/subscriptions` - Manage Subscription
- `PUT /api/v1/organizations/{id}/branding` - Update Branding

### 🎫 Event Service (Port 8083)
- `POST /api/v1/events` - Create Event
- `GET /api/v1/events/{id}` - Get Event Details
- `PUT /api/v1/events/{id}` - Update Event
- `GET /api/v1/events` - List Events
- `DELETE /api/v1/events/{id}` - Delete Event
- `POST /api/v1/events/{id}/schedules` - Create Event Schedule
- `POST /api/v1/events/{id}/promo-codes` - Create Promo Code

### 📦 Order Service (Port 8084)
- `POST /api/v1/orders` - Create Order
- `GET /api/v1/orders/{id}` - Get Order Details
- `GET /api/v1/orders` - List Orders
- `PUT /api/v1/orders/{id}` - Update Order Status
- `POST /api/v1/orders/{id}/cancel` - Cancel Order
- `GET /api/v1/orders/{id}/items` - Get Order Items

### 🎟️ Ticket Service (Port 8085)
- `POST /api/v1/tickets` - Create Tickets
- `GET /api/v1/tickets/{id}` - Get Ticket Details
- `GET /api/v1/tickets` - List Tickets
- `POST /api/v1/tickets/{id}/transfer` - Transfer Ticket
- `POST /api/v1/tickets/{id}/validate` - Validate Ticket
- `POST /api/v1/tickets/{id}/qr-code` - Generate QR Code

### 💳 Payment Service (Port 8086)
- `POST /api/v1/payments` - Process Payment
- `GET /api/v1/payments/{id}` - Get Payment Status
- `POST /api/v1/payments/{id}/refund` - Refund Payment
- `GET /api/v1/payments/history/{userId}` - Payment History

### 📧 Notification Service (Port 8087)
- `POST /api/v1/notifications/email` - Send Email
- `GET /api/v1/notifications` - Get Notifications
- `PUT /api/v1/notifications/{id}/read` - Mark as Read
- `DELETE /api/v1/notifications/{id}` - Delete Notification

### 📊 Analytics Service (Port 8088)
- `GET /api/v1/analytics/sales` - Sales Analytics
- `GET /api/v1/analytics/events` - Event Analytics
- `GET /api/v1/analytics/users` - User Analytics
- `POST /api/v1/analytics/export` - Export Report

---

## ✅ Testing Workflow

### Flow 1: SuperAdmin - Full System Setup
1. **Login** → Use SuperAdmin credentials
2. **Create Organization** → `POST /api/v1/organizations`
3. **Create Event** → `POST /api/v1/events`
4. **Create Event Schedule** → `POST /api/v1/events/{id}/schedules`
5. **Create Promo Code** → `POST /api/v1/events/{id}/promo-codes`
6. **View Analytics** → `GET /api/v1/analytics/sales`

### Flow 2: Admin Org - Event Management
1. **Login** → Use org_admin credentials
2. **Get Organization** → `GET /api/v1/organizations/{id}`
3. **Update Organization** → `PUT /api/v1/organizations/{id}`
4. **List Events** → `GET /api/v1/events`
5. **Create Event Schedule** → `POST /api/v1/events/{id}/schedules`
6. **View Organization Analytics** → `GET /api/v1/analytics/events`

### Flow 3: Guest - Ticket Purchase
1. **Login** → Use guest credentials
2. **List Events** → `GET /api/v1/events`
3. **Get Event Details** → `GET /api/v1/events/{id}`
4. **Create Order** → `POST /api/v1/orders`
5. **Process Payment** → `POST /api/v1/payments`
6. **Get Tickets** → `GET /api/v1/tickets`
7. **Get QR Code** → `POST /api/v1/tickets/{id}/qr-code`

### Flow 4: Scanner - Ticket Validation
1. **Login** → Use scanner credentials
2. **List Tickets** → `GET /api/v1/tickets`
3. **Validate Ticket** → `POST /api/v1/tickets/{id}/validate`
4. **Get Ticket Details** → `GET /api/v1/tickets/{id}`

---

## 🔧 Common Issues & Solutions

### ❌ 401 Unauthorized
**Problem**: Token expired or invalid  
**Solution**:
1. Go to **Auth** folder
2. Click **Login** request
3. Click **Send** again
4. Token will be refreshed

### ❌ 404 Not Found
**Problem**: Service not running  
**Solution**:
```bash
# Check service status
docker-compose ps

# Restart services if needed
docker-compose restart
```

### ❌ Connection Refused
**Problem**: Services not accessible  
**Solution**:
1. Verify Docker is running: `docker ps`
2. Check if containers are running: `docker-compose ps`
3. Verify ports: `netstat -an | grep LISTEN`
4. Restart all services: `docker-compose down && docker-compose up -d`

### ❌ CORS Error
**Problem**: Cross-origin request blocked  
**Solution**: This is handled by API Gateway - ensure it's running

---

## 💡 Tips & Best Practices

1. **Always Login First** - Get fresh token before testing endpoints
2. **Check Environment Variables** - Ensure {{base_url}} and {{auth_url}} are set
3. **Use Pre-request Scripts** - Automatically handles token refresh
4. **Save Responses** - Use environment variables to capture IDs
5. **Test in Order** - Follow the workflow flows above
6. **Monitor Logs** - Check `docker-compose logs -f` for errors

---

## 📞 Need Help?

- **API Documentation**: See `/docs/flow/` folder
- **Service Details**: Check `BACKEND_STARTUP_GUIDE.md`
- **Flow Details**: See `docs/flow/APPLICATION_FLOWS_WITH_ENDPOINTS.md`
- **Docker Issues**: Check container logs: `docker-compose logs <service-name>`

---

## ✨ You're All Set!

Your Postman collection is ready with:
- ✅ 150+ endpoints
- ✅ 4 test user accounts
- ✅ Automatic authentication
- ✅ Pre-configured requests
- ✅ Example request bodies

**Happy Testing! 🚀**
