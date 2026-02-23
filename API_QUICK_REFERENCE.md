# 🚀 Tisqra Platform - API Quick Reference Card

## 📍 Service Locations

```
┌─────────────────────────────────────────────────────────────┐
│                   TISQRA PLATFORM SERVICES                  │
├─────────────────────────────────────────────────────────────┤
│ 🌐 API Gateway               → http://localhost:8080         │
│ 🔍 Discovery Service (Eureka)→ http://localhost:8761        │
│ ⚙️  Config Server            → http://localhost:8888         │
│ 🔐 Keycloak                  → http://localhost:8180         │
├─────────────────────────────────────────────────────────────┤
│ 👤 User Service              → http://localhost:8081         │
│ 🏢 Organization Service      → http://localhost:8082         │
│ 🎭 Event Service             → http://localhost:8083         │
│ 📦 Order Service             → http://localhost:8084         │
│ 🎫 Ticket Service            → http://localhost:8085         │
│ 💳 Payment Service           → http://localhost:8086         │
│ 📧 Notification Service      → http://localhost:8087         │
│ 📈 Analytics Service         → http://localhost:8088         │
├─────────────────────────────────────────────────────────────┤
│ 🐘 PostgreSQL                → localhost:5432 (postgres/root)│
│ 🔴 Redis                     → localhost:6379 (password:root)│
│ 📨 Kafka                     → localhost:9092                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Get Authentication Token

```bash
curl -X POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tisqra-client" \
  -d "username=admin" \
  -d "password=admin"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300
}
```

---

## 📋 Copy-Paste API Commands

### User Service (8081)

#### Get All Users
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/v1/users
```

#### Create User
```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "password": "SecurePassword123!"
  }'
```

#### Get User by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/v1/users/1
```

#### Update User
```bash
curl -X PUT http://localhost:8081/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "email": "user@example.com",
    "firstName": "Jane",
    "lastName": "Doe"
  }'
```

#### Delete User
```bash
curl -X DELETE http://localhost:8081/api/v1/users/1 \
  -H "Authorization: Bearer TOKEN"
```

---

### Organization Service (8082)

#### Get All Organizations
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8082/api/v1/organizations
```

#### Create Organization
```bash
curl -X POST http://localhost:8082/api/v1/organizations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "name": "Tech Events Inc",
    "email": "info@techevents.com",
    "country": "US",
    "city": "San Francisco"
  }'
```

#### Get Organization by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8082/api/v1/organizations/1
```

---

### Event Service (8083)

#### Get All Events
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8083/api/v1/events
```

#### Create Event
```bash
curl -X POST http://localhost:8083/api/v1/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "title": "Tech Conference 2026",
    "description": "Annual technology conference",
    "startDate": "2026-06-15T09:00:00Z",
    "endDate": "2026-06-17T18:00:00Z",
    "location": "San Francisco Convention Center",
    "organizationId": 1,
    "capacity": 5000,
    "currency": "USD"
  }'
```

#### Get Event by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8083/api/v1/events/1
```

---

### Order Service (8084)

#### Get All Orders
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8084/api/v1/orders
```

#### Create Order
```bash
curl -X POST http://localhost:8084/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "userId": 1,
    "eventId": 1,
    "ticketQuantity": 2,
    "ticketType": "STANDARD"
  }'
```

#### Get Order by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8084/api/v1/orders/1
```

---

### Ticket Service (8085)

#### Get All Tickets
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8085/api/v1/tickets
```

#### Get Ticket by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8085/api/v1/tickets/1
```

#### Transfer Ticket
```bash
curl -X POST http://localhost:8085/api/v1/tickets/1/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "toUserId": 2,
    "transferMessage": "Enjoy the event!"
  }'
```

#### Validate Ticket
```bash
curl -X POST http://localhost:8085/api/v1/tickets/1/validate \
  -H "Authorization: Bearer TOKEN"
```

#### Get QR Code
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8085/api/v1/tickets/1/qr-code
```

---

### Payment Service (8086)

#### Create Payment
```bash
curl -X POST http://localhost:8086/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "orderId": 1,
    "amount": 199.99,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "cvv": "123"
  }'
```

#### Get Payment by ID
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8086/api/v1/payments/1
```

#### Refund Payment
```bash
curl -X POST http://localhost:8086/api/v1/payments/1/refund \
  -H "Authorization: Bearer TOKEN"
```

---

### Notification Service (8087)

#### Send Email Notification
```bash
curl -X POST http://localhost:8087/api/v1/notifications/email \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "recipientEmail": "user@example.com",
    "subject": "Event Confirmation",
    "templateName": "ticket-purchase",
    "variables": {
      "eventName": "Tech Conference 2026",
      "ticketNumber": "TECH001"
    }
  }'
```

---

### Analytics Service (8088)

#### Get Sales Analytics
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8088/api/v1/analytics/sales
```

#### Get Event Analytics
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8088/api/v1/analytics/events
```

#### Get Dashboard Metrics
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8088/api/v1/analytics/dashboard
```

---

## 🧪 Health Checks

```bash
# Check all services
echo "User Service:"
curl http://localhost:8081/actuator/health

echo "Organization Service:"
curl http://localhost:8082/actuator/health

echo "Event Service:"
curl http://localhost:8083/actuator/health

echo "Order Service:"
curl http://localhost:8084/actuator/health

echo "Ticket Service:"
curl http://localhost:8085/actuator/health

echo "Payment Service:"
curl http://localhost:8086/actuator/health

echo "Notification Service:"
curl http://localhost:8087/actuator/health

echo "Analytics Service:"
curl http://localhost:8088/actuator/health
```

---

## 📖 Swagger UI Links

Open in browser:

| Service | Link |
|---------|------|
| User | http://localhost:8081/swagger-ui.html |
| Organization | http://localhost:8082/swagger-ui.html |
| Event | http://localhost:8083/swagger-ui.html |
| Order | http://localhost:8084/swagger-ui.html |
| Ticket | http://localhost:8085/swagger-ui.html |
| Payment | http://localhost:8086/swagger-ui.html |
| Notification | http://localhost:8087/swagger-ui.html |
| Analytics | http://localhost:8088/swagger-ui.html |

---

## 🗄️ Database Access

### PostgreSQL CLI
```bash
docker exec -it eventticket-postgres psql -U postgres -d eventticket_db
```

### Common Queries
```sql
-- List all databases
\l

-- List all tables
\dt

-- Select from users table
SELECT * FROM users;

-- Select from events table
SELECT * FROM events;

-- Count orders
SELECT COUNT(*) FROM orders;
```

---

## 🐳 Docker Commands

```bash
# View all services
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f user-service

# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart user-service

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Start services
docker-compose up -d
```

---

## 🔧 Common Troubleshooting

### Service Not Responding
```bash
# Check if service is running
docker-compose ps | grep service-name

# Check logs
docker-compose logs service-name

# Restart service
docker-compose restart service-name
```

### Database Connection Error
```bash
# Check PostgreSQL
docker-compose logs postgres

# Verify credentials in .env file
cat .env | grep POSTGRES
```

### Keycloak Token Invalid
```bash
# Get new token
curl -X POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=tisqra-client&username=admin&password=admin"
```

---

## 💡 Pro Tips

### 1. Save Token to Variable
```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=tisqra-client&username=admin&password=admin" \
  | jq -r '.access_token')

echo $TOKEN
```

### 2. Pretty Print JSON
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/v1/users | jq .
```

### 3. Save Response to File
```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/v1/users > users.json
```

### 4. Test with Parameters
```bash
curl "http://localhost:8081/api/v1/users?page=0&size=10" \
  -H "Authorization: Bearer TOKEN"
```

---

## 📚 File References

- **Postman Collection**: `postman/Tisqra-Complete-API.postman_collection.json`
- **Complete Testing Guide**: `API_TESTING_GUIDE.md`
- **Swagger Documentation**: `SWAGGER_ENDPOINTS.md`
- **Backend Setup Guide**: `BACKEND_STARTUP_GUIDE.md`

---

## ✅ API Testing Checklist

- [ ] Got access token from Keycloak
- [ ] Tested User Service health check
- [ ] Created a new user
- [ ] Tested Organization Service health check
- [ ] Created a new organization
- [ ] Tested Event Service health check
- [ ] Created a new event
- [ ] Tested Order Service health check
- [ ] Created an order
- [ ] Tested Payment Service health check
- [ ] Processed a payment
- [ ] Tested Ticket Service health check
- [ ] Retrieved tickets
- [ ] Tested Notification Service health check
- [ ] Tested Analytics Service health check
- [ ] Opened Swagger UI for all services

---

**Happy API Testing! 🎉**

*Last Updated: 2026-02-23*
