# Postman Testing Workflow

## Setup Instructions

### 1. Import Postman Collection
- Download `Tisqra-Complete-User-Flows.postman_collection.json`
- Open Postman
- Click `Import` → Select the JSON file
- Collection will appear in left sidebar

### 2. Create Environment Variables
Create a new environment called "Tisqra Development":

```
{
  "baseUrl": "http://localhost:8080",
  "gatewayUrl": "http://localhost:8080",
  "keycloakUrl": "http://localhost:8180",
  "accessToken": "",
  "refreshToken": "",
  "userId": "",
  "organizationId": "",
  "eventId": "",
  "orderId": "",
  "ticketId": ""
}
```

### 3. Set Active Environment
- Click environment dropdown (top right)
- Select "Tisqra Development"

## Collection Structure

```
Tisqra Complete User Flows
├── Authentication
│   ├── Register User
│   ├── Login
│   ├── Refresh Token
│   └── Logout
├── SuperAdmin Operations
│   ├── List All Users
│   ├── Create Admin User
│   ├── List Organizations
│   └── System Statistics
├── Organization Admin
│   ├── Create Organization
│   ├── Update Organization
│   ├── Create Event
│   ├── Update Event
│   └── View Analytics
├── Guest User
│   ├── Browse Events
│   ├── Search Events
│   ├── View Event Details
│   ├── Purchase Tickets
│   └── View My Orders
├── Scanner
│   ├── Authenticate Scanner
│   ├── Scan QR Code
│   ├── Verify Ticket
│   └── Mark Attendance
└── Utility
    ├── Health Check
    ├── Get System Stats
    └── View Service Status
```

## Testing Workflows

### Workflow 1: Guest User Journey

#### Step 1: Register
- **Request**: `POST {{baseUrl}}/auth/register`
- **Body**:
```json
{
  "email": "guest@test.com",
  "password": "GuestPass123!",
  "firstName": "Guest",
  "lastName": "User"
}
```
- **Save Response**: Extract `userId` → save to `{{userId}}`

#### Step 2: Login
- **Request**: `POST {{baseUrl}}/auth/login`
- **Body**:
```json
{
  "email": "guest@test.com",
  "password": "GuestPass123!"
}
```
- **Save Response**: Extract `accessToken` → save to `{{accessToken}}`

#### Step 3: Browse Events
- **Request**: `GET {{baseUrl}}/events`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Verify**: Response contains event list

#### Step 4: View Event Details
- **Request**: `GET {{baseUrl}}/events/{{eventId}}`
- **Verify**: All event details are returned

#### Step 5: Purchase Tickets
- **Request**: `POST {{baseUrl}}/orders`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Body**:
```json
{
  "eventId": "{{eventId}}",
  "tickets": [{
    "ticketId": "{{ticketId}}",
    "quantity": 2
  }],
  "paymentMethod": "CARD"
}
```
- **Save Response**: Extract `orderId` → save to `{{orderId}}`

#### Step 6: View Order
- **Request**: `GET {{baseUrl}}/orders/{{orderId}}`
- **Verify**: Order details match purchase

### Workflow 2: Organization Admin Journey

#### Step 1: Register as Admin
- **Request**: `POST {{baseUrl}}/auth/register`
- **Body**:
```json
{
  "email": "admin@org.com",
  "password": "AdminPass123!",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN_ORG"
}
```

#### Step 2: Login
- **Request**: `POST {{baseUrl}}/auth/login`
- **Body**: Use admin credentials
- **Save**: `accessToken`

#### Step 3: Create Organization
- **Request**: `POST {{baseUrl}}/organizations`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Body**:
```json
{
  "name": "Test Organization",
  "email": "org@test.com",
  "phoneNumber": "+1234567890",
  "address": "123 Test St"
}
```
- **Save**: `organizationId`

#### Step 4: Create Event
- **Request**: `POST {{baseUrl}}/events`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Body**:
```json
{
  "title": "Test Event",
  "description": "A test event",
  "startDate": "2024-12-31T18:00:00Z",
  "endDate": "2024-12-31T22:00:00Z",
  "location": "Test Venue",
  "organizationId": "{{organizationId}}",
  "capacity": 100
}
```
- **Save**: `eventId`

#### Step 5: Create Tickets
- **Request**: `POST {{baseUrl}}/tickets`
- **Body**:
```json
{
  "eventId": "{{eventId}}",
  "ticketType": "STANDARD",
  "price": 50.00,
  "quantity": 100,
  "description": "Standard Ticket"
}
```

#### Step 6: View Analytics
- **Request**: `GET {{baseUrl}}/analytics/events/{{eventId}}`
- **Verify**: Analytics data is populated

### Workflow 3: Scanner Journey

#### Step 1: Authenticate Scanner
- **Request**: `POST {{baseUrl}}/auth/login`
- **Body**:
```json
{
  "email": "scanner@event.com",
  "password": "ScannerPass123!"
}
```

#### Step 2: Scan QR Code
- **Request**: `POST {{baseUrl}}/tickets/scan`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Body**:
```json
{
  "qrCode": "QR_CODE_DATA",
  "eventId": "{{eventId}}"
}
```

#### Step 3: Mark Attendance
- **Request**: `PUT {{baseUrl}}/tickets/{{ticketId}}/attended`
- **Body**:
```json
{
  "eventId": "{{eventId}}"
}
```

### Workflow 4: SuperAdmin Journey

#### Step 1: Login as SuperAdmin
- **Request**: `POST {{baseUrl}}/auth/login`
- **Body**:
```json
{
  "email": "superadmin@tisqra.com",
  "password": "SuperAdminPass123!"
}
```

#### Step 2: List All Users
- **Request**: `GET {{baseUrl}}/users?page=0&size=10`
- **Headers**: `Authorization: Bearer {{accessToken}}`

#### Step 3: List All Organizations
- **Request**: `GET {{baseUrl}}/organizations?page=0&size=10`

#### Step 4: View System Statistics
- **Request**: `GET {{baseUrl}}/admin/statistics`

#### Step 5: Create New Admin
- **Request**: `POST {{baseUrl}}/users/admin`
- **Body**:
```json
{
  "email": "newadmin@tisqra.com",
  "password": "NewAdminPass123!",
  "role": "ADMIN"
}
```

## Pre-request Scripts

### Generate Timestamp
```javascript
pm.environment.set("timestamp", new Date().toISOString());
```

### Set Authorization Header
```javascript
const token = pm.environment.get("accessToken");
pm.request.headers.add({
  key: "Authorization",
  value: "Bearer " + token
});
```

## Tests (Assertions)

### Check Response Status
```javascript
pm.test("Status code is 200", function() {
  pm.response.to.have.status(200);
});
```

### Save Token from Response
```javascript
pm.test("Save access token", function() {
  const jsonData = pm.response.json();
  pm.environment.set("accessToken", jsonData.accessToken);
});
```

### Verify Response Schema
```javascript
pm.test("Response has required fields", function() {
  const jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("id");
  pm.expect(jsonData).to.have.property("email");
});
```

## Error Handling

### 401 Unauthorized
- Refresh token: `POST {{baseUrl}}/auth/refresh`
- Re-run request with new token

### 404 Not Found
- Verify resource ID exists
- Check collection/environment variables

### 500 Server Error
- Check backend service logs
- Verify all services are running
- Check database connectivity

## Performance Testing

### Response Time Test
```javascript
pm.test("Response time is less than 2000ms", function() {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### Load Testing with Collection Runner
1. Select collection
2. Click "Run"
3. Set iterations: 10
4. Set delay: 100ms
5. Monitor results

## Exporting Results

1. Collection Runner → Export Results
2. Choose format (JSON/HTML)
3. Save report
4. Share with team

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Verify services running on correct ports |
| CORS error | Check API Gateway configuration |
| Token expired | Refresh token using refresh endpoint |
| Invalid JSON | Validate JSON in request body |
| Missing header | Add Authorization header with Bearer token |

## Best Practices

1. **Use Environment Variables**: Never hardcode sensitive data
2. **Save IDs**: Always save important IDs for use in subsequent requests
3. **Test in Order**: Follow workflows sequentially
4. **Monitor Performance**: Track response times
5. **Document Changes**: Update workflows if APIs change
6. **Version Control**: Keep Postman collection in git
7. **Review Responses**: Always check response structure

## Next Steps
- Automate workflows with Collection Runner
- Setup CI/CD integration
- Create performance baselines
- Document API changes
- Train team on Postman testing
