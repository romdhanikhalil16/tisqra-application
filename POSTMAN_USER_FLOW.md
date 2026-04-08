# TISQRA Platform API (User-flow ordered)

Base URL (Gateway): `http://localhost:8080`

All APIs return a wrapper:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## Roles & seeded accounts (Keycloak)

Realm: `event-ticketing` (imported from `docker/keycloak/realm-export.json`)

- **SuperAdmin**: `admin@eventticketing.com` / `admin123` (role `SUPER_ADMIN`)
- **AdminOrg**: `organizer@eventticketing.com` / `organizer123` (role `ADMIN_ORG`)
- **Scanner**: `scanner@eventticketing.com` / `scanner123` (role `SCANNER`)
- **Guest**: `guest@eventticketing.com` / `guest123` (role `GUEST`)

## 0) Authentication

### Register (public → Guest by default)

`POST /api/auth/register`

Request:

```json
{
  "username": "guest@eventticketing.com",
  "email": "guest@eventticketing.com",
  "password": "guest12345",
  "firstName": "Regular",
  "lastName": "User",
  "phone": "+21600000000",
  "role": "GUEST"
}
```

Response (example):

```json
{
  "success": true,
  "data": {
    "id": "8d1c9d3e-3b7a-4d0a-9d7a-1c6c7f2b4b0a",
    "email": "guest@eventticketing.com",
    "firstName": "Regular",
    "lastName": "User",
    "phone": "+21600000000",
    "role": "GUEST",
    "isActive": true,
    "emailVerified": false
  },
  "error": null
}
```

### Login (all roles)

`POST /api/auth/login`

Request:

```json
{
  "email": "admin@eventticketing.com",
  "password": "admin123"
}
```

Response (example):

```json
{
  "success": true,
  "data": {
    "accessToken": "<jwt>",
    "refreshToken": "<jwt>",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "…",
      "email": "admin@eventticketing.com",
      "firstName": "Super",
      "lastName": "Admin",
      "role": "SUPER_ADMIN"
    }
  },
  "error": null
}
```

### Logout

`POST /api/auth/logout?userId={uuid}`

Headers: `Authorization: Bearer <accessToken>`

### Verify email

`POST /api/auth/email/verify?token={token}`

### Password reset

- `POST /api/auth/password/reset-request?email={email}`
- `POST /api/auth/password/reset`

Request:

```json
{ "token": "<token>", "newPassword": "NewPass123!" }
```

## 1) User management (role-based)

Base: `/api/users`

- **SuperAdmin** can list + activate/deactivate.
- **AdminOrg** can list (per controller), and fetch by email.
- **Guest/Scanner** typically only fetch self (note: your controller compares `id` with JWT `sub` claim; see note below).

### Get all users

`GET /api/users?page=0&size=20` (requires `SUPER_ADMIN` or `ADMIN_ORG`)

### Get user by id

`GET /api/users/{id}`

### Get user by email (admin-only)

`GET /api/users/email/{email}`

### Get user by keycloak id

`GET /api/users/keycloak/{keycloakId}`

## 2) Organization management (AdminOrg / SuperAdmin)

Base: `/api/organizations`

- `POST /api/organizations`
- `GET /api/organizations?page=0&size=20`
- `GET /api/organizations/{id}`
- `PUT /api/organizations/{id}`

## 3) Event management

Base: `/api/events`

Public:

- `GET /api/events/{id}`
- `GET /api/events/slug/{slug}`
- `GET /api/events/organization/{organizationId}`
- `GET /api/events/upcoming`
- `GET /api/events/category/{category}`
- `GET /api/events/search?query=...`

Admin:

- `POST /api/events` (roles: `SUPER_ADMIN`, `ADMIN_ORG`)
- `PUT /api/events/{id}` (roles: `SUPER_ADMIN`, `ADMIN_ORG`)
- `POST /api/events/{id}/publish` (roles: `SUPER_ADMIN`, `ADMIN_ORG`)
- `POST /api/events/{id}/cancel` (roles: `SUPER_ADMIN`, `ADMIN_ORG`)

## 4) Ordering & payments (Guest)

Orders base: `/api/orders`

- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/number/{orderNumber}`
- `GET /api/orders/user/{userId}`
- `GET /api/orders/event/{eventId}` (admin)
- `POST /api/orders/{id}/confirm`
- `POST /api/orders/{id}/complete`
- `POST /api/orders/{id}/cancel`
- `POST /api/orders/{id}/refund` (admin)

Payments base: `/api/payments`

- `POST /api/payments/process`
- `GET /api/payments/{id}`
- `GET /api/payments/order/{orderId}`
- `POST /api/payments/{id}/refund` (admin)
- `GET /api/payments/verify/{providerPaymentId}` (admin)

## 5) QR ticket generation & validation

Tickets base: `/api/tickets`

- `POST /api/tickets/generate`
- `GET /api/tickets/{ticketId}`
- `GET /api/tickets/order/{orderId}`
- `GET /api/tickets/user/{userId}`
- `POST /api/tickets/validate?qrCode=...&scannerId=...&scannerName=...`
- `POST /api/tickets/{ticketId}/transfer`
- `POST /api/tickets/{ticketId}/cancel`

## 6) Reports & analytics (AdminOrg / SuperAdmin)

Analytics base: `/api/analytics`

- `GET /api/analytics/dashboard/{organizationId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- `GET /api/analytics/event/{eventId}/sales?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

## Notes / gotchas

- The mobile app stores user **DB id** separately from Keycloak `sub` and fetches it via `GET /api/users/keycloak/{sub}`.
- In `UserController`, some access rules compare `{id}` to JWT `sub`. If `{id}` is your DB UUID (not Keycloak `sub`), that check will never pass for normal users; admins will still work. If you want “self profile” access, add a dedicated endpoint like `GET /api/users/me` that resolves by `sub`.

