# Complete Testing Flow Guide

## Overview
This guide provides step-by-step instructions for testing all Tisqra Platform features end-to-end.

## Prerequisites
- Postman installed
- All backend services running
- Keycloak configured
- Valid test credentials

## Testing Scenarios

### 1. User Registration & Authentication Flow
**Endpoint**: `POST /auth/register`
```json
{
  "email": "test@example.com",
  "password": "TestPassword123!",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "+1234567890"
}
```

**Expected Response**: 201 Created
```json
{
  "id": "uuid",
  "email": "test@example.com",
  "firstName": "Test",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### 2. Login Flow
**Endpoint**: `POST /auth/login`
```json
{
  "email": "test@example.com",
  "password": "TestPassword123!"
}
```

**Expected Response**: 200 OK
```json
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token",
  "expiresIn": 3600,
  "userId": "uuid"
}
```

### 3. Create Organization
**Endpoint**: `POST /organizations`
**Headers**: 
- `Authorization: Bearer {accessToken}`
- `Content-Type: application/json`

```json
{
  "name": "Test Organization",
  "description": "Test Org Description",
  "email": "org@test.com",
  "phoneNumber": "+1234567890",
  "address": "123 Test Street"
}
```

### 4. Create Event
**Endpoint**: `POST /events`
**Headers**: 
- `Authorization: Bearer {accessToken}`

```json
{
  "title": "Test Event",
  "description": "Test Event Description",
  "startDate": "2024-12-31T18:00:00Z",
  "endDate": "2024-12-31T22:00:00Z",
  "location": "Test Venue",
  "organizationId": "org-uuid",
  "capacity": 100,
  "categoryId": "category-uuid"
}
```

### 5. Create Tickets
**Endpoint**: `POST /tickets`

```json
{
  "eventId": "event-uuid",
  "ticketType": "STANDARD",
  "price": 50.00,
  "quantity": 100,
  "description": "Standard Ticket"
}
```

### 6. Purchase Tickets
**Endpoint**: `POST /orders`

```json
{
  "eventId": "event-uuid",
  "tickets": [
    {
      "ticketId": "ticket-uuid",
      "quantity": 2
    }
  ],
  "paymentMethod": "CARD"
}
```

### 7. Process Payment
**Endpoint**: `POST /payments`

```json
{
  "orderId": "order-uuid",
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "cardDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

## Verification Checklist

- [ ] User can register successfully
- [ ] User can login and receive JWT token
- [ ] Organization can be created
- [ ] Event can be created within organization
- [ ] Tickets can be created for event
- [ ] Orders can be created
- [ ] Payments are processed correctly
- [ ] Notifications are sent
- [ ] Tickets are transferred successfully
- [ ] QR codes are generated
- [ ] Analytics data is recorded

## Common Issues & Solutions

### Issue: Invalid Token
**Solution**: Ensure token hasn't expired. Refresh using refresh token endpoint.

### Issue: Organization Not Found
**Solution**: Verify organizationId exists and user has access.

### Issue: Payment Declined
**Solution**: Use valid test card numbers. Check payment service logs.

## Performance Testing

### Load Testing Scenario
- 100 concurrent users
- Each creates 1 order
- Expected response time: < 2 seconds

### Stress Testing
- 1000 concurrent requests
- Monitor service health
- Check database connections

## Database Verification

```sql
-- Verify user creation
SELECT * FROM users WHERE email = 'test@example.com';

-- Verify organization
SELECT * FROM organizations WHERE name = 'Test Organization';

-- Verify event
SELECT * FROM events WHERE title = 'Test Event';

-- Verify tickets
SELECT * FROM tickets WHERE event_id = 'event-uuid';

-- Verify orders
SELECT * FROM orders WHERE user_id = 'user-uuid';

-- Verify payments
SELECT * FROM payments WHERE order_id = 'order-uuid';
```

## Rollback Instructions

If testing fails, rollback using:

```bash
# Delete test organization
DELETE FROM organizations WHERE name = 'Test Organization';

# Delete test user
DELETE FROM users WHERE email = 'test@example.com';

# Truncate tables if needed
TRUNCATE TABLE test_data CASCADE;
```

## Next Steps
- Automate tests using Postman collections
- Setup CI/CD pipeline
- Monitor production metrics
- Document edge cases
