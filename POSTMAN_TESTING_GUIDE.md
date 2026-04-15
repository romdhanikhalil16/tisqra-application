# TISQRA API Testing Guide (Postman)

This guide is designed to verify the Spring Boot microservices behind the API Gateway, end-to-end:
- Auth/User management
- Organizations/Subscriptions
- Events
- Promo codes + Ticket categories
- Orders + Payments
- Ticket generation + QR validation + Transfer/Cancel
- Notifications
- Analytics

All endpoints below refer to the API Gateway base URL (default: `{{BASE_URL}}`).

---

## 1. Prerequisites / Setup

### 1.1 Start services
Use your existing Docker Compose / Jenkins workflow to bring the platform up.

### 1.1.1 PostgreSQL port mapping (critical for pgAdmin / manual DB edits)
In `docker-compose.yml`, Postgres is exposed as **host `5433` → container `5432`**.

- If you're connecting from your host (pgAdmin, DBeaver, `psql` on Windows), use:
  - **Host**: `localhost`
  - **Port**: `5433`
  - **Database**: `user_db` (for user-service)
  - **User**: `postgres` (default)
  - **Password**: `root` (default)

If you accidentally connect pgAdmin to `localhost:5432`, you may be looking at a *different* Postgres instance than the one the services use, which can cause confusing behavior like “user already exists” after you “deleted” the row.

#### Quick SQL to confirm you're in the right database
Run this in pgAdmin Query Tool (connected to `user_db`) **before and after** deleting a user:

```sql
select current_database() as db,
       current_schema() as schema,
       inet_server_addr() as server_addr,
       inet_server_port() as server_port;

select count(*) as users_with_email
from users
where lower(trim(email)) = lower(trim('<EMAIL_HERE>'));
```

Docker Compose includes **MailHog** for local SMTP (no real mailbox required):
- SMTP: `localhost:1025` (from your host) or hostname `mailhog:1025` from containers
- Web UI to read messages: `http://localhost:8025`

`user-service` and `notification-service` default to `EMAIL_HOST=mailhog` in the `docker` profile so verification and notification emails are delivered to MailHog unless you override `EMAIL_*` with a real provider (Brevo, SendGrid, etc.).

### 1.2 Verify the Gateway is reachable
In Postman, run a quick check:
- `GET {{BASE_URL}}/actuator/health`

Expected: HTTP `200` with service status.

### 1.3 Postman variables you should set
Set (or override) these collection/environment variables:
- `BASE_URL` (e.g., `http://192.168.x.x:8080` for phone)
- `ACCESS_TOKEN` (after login)
- `USER_ID`
- `ORG_ID`
- `EVENT_ID`
- `ORDER_ID`
- `ORDER_NUMBER` (optional but used by one endpoint)
- `PAYMENT_ID` and `PAYMENT_PROVIDER_PAYMENT_ID` (if you want to test verify/refund)
- `TICKET_ID`
- `TICKET_CATEGORY_ID`
- `NOTIFICATION_ID`
- `SCANNER_ID` (UUID)
- `QR_CODE` (string)
- `PROMO_CODE` (e.g., `SAVE10`)
- `PROMO_CODE_ID`
- `PAGE` and `SIZE` (default `0` and `20`)
- `START_DATE` / `END_DATE` in `YYYY-MM-DD` format (for analytics)
- `VERIFY_TOKEN` (paste from verification email link after Register or Resend)
- `RESET_TOKEN`, `NEW_PASSWORD` (for password reset flow)

### 1.4 Expected response shape (most endpoints)
Most protected controllers return:
```json
{
  "success": true,
  "data": { "..." : "..." },
  "error": null
}
```
If an endpoint returns a primitive (like `boolean` for subscription check), then it won’t be wrapped in `ApiResponse`.

### 1.5 Auth payload contract (important)
For `POST {{BASE_URL}}/api/auth/register`, the backend now accepts both:
- `role` (preferred)
- `userRole` (backward-compatible alias)

Role values are case-insensitive (`guest`, `GUEST`, `Guest` are all valid and normalized to `GUEST`).
Public registration is restricted to `GUEST` only. Any attempt to register `SUPER_ADMIN`, `ADMIN_ORG`, or `SCANNER` through this endpoint returns `400`.

Example (frontend-friendly):
```json
{
  "username": "newguest@example.com",
  "email": "newguest@example.com",
  "password": "Guest12345!",
  "firstName": "New",
  "lastName": "Guest",
  "userRole": "guest"
}
```

For `POST {{BASE_URL}}/api/auth/login`, only `email` and `password` are required. Email is normalized (trimmed + lowercased) before authentication and DB lookup.
Both register and login now return token payload under `data`:
```json
{
  "success": true,
  "data": {
    "user": { "...": "..." },
    "access_token": "<jwt>",
    "token_type": "Bearer"
  },
  "error": null
}
```

### 1.6 Role restrictions (mobile + API)
- Regular mobile auth screens (`/login`, `/register`) are for attendee flow only.
- `SUPER_ADMIN` and `ADMIN_ORG` are blocked from regular mobile login with a clear message.
- Public registration always creates `GUEST` accounts.
- Creating `ADMIN_ORG` users via `POST /api/users` is restricted to `SUPER_ADMIN` at API level.
- `SCANNER` and `ADMIN_ORG` accounts must be provisioned by `SUPER_ADMIN` (not by public register flow).

### 1.7 Auth flow details (important for Postman)
Email verification is DB-backed and sent over **real SMTP** (MailHog in Docker dev, or your provider in prod).

**Recommended order in Postman**
1. **Register** — `POST {{BASE_URL}}/api/auth/register` → `201`, sets collection `EMAIL` + `USER_ID` (test script).
2. **Read the verification email** — MailHog UI `http://localhost:8025` (Docker), or your real inbox (prod). Copy only the **token** value (everything after `token=` in the link), or paste the **entire URL** into `VERIFY_TOKEN` (the API normalizes both).
3. **Verify email** — `POST {{BASE_URL}}/api/auth/email/verify?token={{VERIFY_TOKEN}}` → `200`.
4. **Login** — use the same email/password as Register (collection **Login** uses `{{EMAIL}}` + `Guest12345!` to match the default Register body). Test script fills `ACCESS_TOKEN` and `USER_ID`.
5. **Logout** — `POST {{BASE_URL}}/api/auth/logout?userId={{USER_ID}}` (must be the **application** user id from login `data.user.id`, not JWT `sub`). Returns `200` if the user exists; Keycloak session revoke is best-effort and does not fail the call.

**Resend** (optional): `POST {{BASE_URL}}/api/auth/email/resend-verification?email={{EMAIL}}` — issues a new token and sends again. Use when the token expired or email was lost.

#### SMTP / Mail environment variables
| Variable | Purpose |
|----------|---------|
| `EMAIL_HOST` | SMTP host (Docker default: `mailhog`) |
| `EMAIL_PORT` | SMTP port (Docker default: `1025`) |
| `EMAIL_USER` / `EMAIL_PASS` | SMTP auth (empty for MailHog) |
| `EMAIL_SMTP_AUTH` | `true` / `false` (MailHog: `false`) |
| `EMAIL_SMTP_STARTTLS` | `true` / `false` (MailHog: `false`) |
| `EMAIL_VERIFICATION_BASE_URL` | Prefix for the link in the email (must end with `token=`; default through gateway: `http://localhost:8080/api/auth/email/verify?token=`) |

**notification-service** (Docker): `spring.mail` reads `EMAIL_*` first, then falls back to `BREVO_*`, so one set of env vars can feed both services.

#### Verify / resend / logout troubleshooting
| Symptom | Likely cause | Fix |
|---------|----------------|-----|
| `400` + “Invalid verification token” | Wrong/pasted link fragment, or token never stored | Use MailHog to confirm the message; run **Resend**; paste full URL or raw token into `VERIFY_TOKEN` |
| `400` + “User not found” on **Resend** | `{{EMAIL}}` does not match a registered user | Run **Register** first (script sets `EMAIL`) or set `EMAIL` manually to the registered address |
| `400` + “Failed to send verification email” | SMTP misconfiguration | For real SMTP set `EMAIL_SMTP_AUTH=true`, `EMAIL_SMTP_STARTTLS=true`, correct host/port/credentials |
| `500` on **Resend** (generic) | Uncaught error (e.g. DB migration missing `verification_token` column) | Rebuild `user-service`, ensure Flyway `V3` applied; check logs |
| `400` on **Logout** | `USER_ID` empty or wrong (e.g. JWT `sub`) | Run **Login** after Register so the test script sets `USER_ID` from `data.user.id` |
| Re-register fails with “user exists” after you deleted the row in pgAdmin | User still exists in **Keycloak** (separate DB) | Delete the user in Keycloak admin UI (realm `event-ticketing`) then register again |

#### Postman collection (`TISQRA_USER_FLOW_aligned.postman_collection.json`)
- Folder **0) Auth** order: Register → Verify → Resend → Login (guest, `{{EMAIL}}` + password matching Register) → **Login (SUPER_ADMIN seed)** for admin flows → Logout.
- **Register** / **Login** / **Logout** include test scripts for status and variable capture.

---

## 2. Seeded Credentials (for quick testing)
These come from `docker/keycloak/realm-export.json`.

- Guest:
  - Email: `guest@eventticketing.com`
  - Password: `guest123`
- Scanner:
  - Email: `scanner@eventticketing.com`
  - Password: `scanner123`
- SUPER_ADMIN:
  - Email: `admin@eventticketing.com`
  - Password: `admin123`
- ADMIN_ORG:
  - Email: `organizer@eventticketing.com`
  - Password: `organizer123`

---

## 3. Shared “Happy Path” Flow (recommended order)
This is the same flow you’ll reuse inside each role section.

1. Login as `ADMIN_ORG` or `SUPER_ADMIN`
2. Create an `Organization` (capture `ORG_ID`)
3. Create an `Event` (capture `EVENT_ID`)
4. From the event response, capture the first `ticketCategories[0].id` (or `categories[0].id`) into `TICKET_CATEGORY_ID`
5. (Optional) Create a `Promo Code` (capture `PROMO_CODE_ID` if you want deactivate later)
6. Login as `GUEST`
7. Create an `Order` using `CreateOrderRequest` (capture `ORDER_ID`)
8. Confirm + Complete the order
9. Generate tickets for the order (capture tickets and set `QR_CODE` / `TICKET_ID`)
10. Login as `SCANNER`
11. Validate the ticket QR code
12. (Optional) Transfer or cancel a ticket
13. Check Notifications + Analytics (as allowed)

---

## 4. Role: Guest (GUEST)

### Scenario G1: Browse events (public, no token)
1. `GET {{BASE_URL}}/api/events/upcoming?page={{PAGE}}&size={{SIZE}}`
2. `GET {{BASE_URL}}/api/events/search?query=tech&page={{PAGE}}&size={{SIZE}}`
3. `GET {{BASE_URL}}/api/events/category/CONFERENCE?page={{PAGE}}&size={{SIZE}}`

Expected:
- HTTP `200`
- `success: true`
- `data` contains paged events

### Scenario G2: Validate promo code (public)
1. Login is NOT required (endpoint is public).
2. `GET {{BASE_URL}}/api/promo-codes/validate?code={{PROMO_CODE}}&eventId={{EVENT_ID}}`

Expected:
- HTTP `200`
- `success: true`
- `data.code`, `data.discountType`, `data.isValid` etc.

### Scenario G3: Login as Guest (get token)
1. `POST {{BASE_URL}}/api/auth/login`
Body:
```json
{
  "email": "guest@eventticketing.com",
  "password": "guest123"
}
```
2. Copy `data.accessToken` into `ACCESS_TOKEN`

Expected:
- HTTP `200`
- Response `data.accessToken` is non-empty

### Scenario G4: Create Order (authenticated)
Endpoint:
`POST {{BASE_URL}}/api/orders`

Body (`CreateOrderRequest`):
```json
{
  "userId": "{{USER_ID}}",
  "eventId": "{{EVENT_ID}}",
  "items": [
    { "ticketCategoryId": "{{TICKET_CATEGORY_ID}}", "quantity": 2 }
  ],
  "promoCode": "{{PROMO_CODE}}"
}
```

Expected:
- HTTP `201`
- `data.id` -> set `ORDER_ID`
- `data.orderNumber` -> optionally set `ORDER_NUMBER`

### Scenario G5: Confirm + Complete order
1. `POST {{BASE_URL}}/api/orders/{{ORDER_ID}}/confirm`
2. `POST {{BASE_URL}}/api/orders/{{ORDER_ID}}/complete`

Expected:
- HTTP `200`
- `success: true` and `data` contains updated `OrderDTO` for confirm/complete

### Scenario G6: Generate tickets (QR strings)
Endpoint:
`POST {{BASE_URL}}/api/tickets/generate`

Body (`GenerateTicketsRequest`):
```json
{ "orderId": "{{ORDER_ID}}" }
```

Expected:
- HTTP `200`
- `data` is a list of `TicketDTO`
- For at least one ticket:
  - set `TICKET_ID` to `data[0].id`
  - set `QR_CODE` to `data[0].qrCode`

### Scenario G7: List user tickets
Endpoint:
`GET {{BASE_URL}}/api/tickets/user/{{USER_ID}}?page={{PAGE}}&size={{SIZE}}`

Expected:
- HTTP `200`
- tickets list contains your generated tickets

### Scenario G8: Transfer ticket (authenticated)
Endpoint:
`POST {{BASE_URL}}/api/tickets/{{TICKET_ID}}/transfer`

Body (`TransferTicketRequest`):
```json
{
  "recipientEmail": "friend@example.com",
  "message": "Enjoy the event!"
}
```

Expected:
- HTTP `200`
- `data.id` still matches ticket
- ticket owner fields updated (depends on service behavior)

### Scenario G9: Cancel ticket (authenticated)
Endpoint:
`POST {{BASE_URL}}/api/tickets/{{TICKET_ID}}/cancel`

Expected:
- HTTP `200`
- `success: true`

### Scenario G10: Notifications lifecycle
1. `GET {{BASE_URL}}/api/notifications/user/{{USER_ID}}?page={{PAGE}}&size={{SIZE}}`
2. Take one notification id into `NOTIFICATION_ID`
3. `POST {{BASE_URL}}/api/notifications/{{NOTIFICATION_ID}}/read`
4. (Optional) `DELETE {{BASE_URL}}/api/notifications/{{NOTIFICATION_ID}}`

Expected:
- `GET` returns notifications page
- `read` sets `read: true`
- `DELETE` returns `ApiResponse<Void>`

### Scenario G12: Ticket-category reserve/release (authenticated)
These endpoints do not have `@PreAuthorize` checks at controller level, so any authenticated role that passes gateway security should work.
1. Reserve:
   - `POST {{BASE_URL}}/api/ticket-categories/{{TICKET_CATEGORY_ID}}/reserve?quantity=2`
2. Release:
   - `POST {{BASE_URL}}/api/ticket-categories/{{TICKET_CATEGORY_ID}}/release?quantity=1`

Expected:
- HTTP `200` for both
- response: `ApiResponse<Void>` with `success: true`

### Scenario G13 (Negative): Unauthorized call should fail (401)
Clear `ACCESS_TOKEN` and re-run one authenticated endpoint.
Example:
- `POST {{BASE_URL}}/api/orders`
Body:
```json
{
  "userId": "{{USER_ID}}",
  "eventId": "{{EVENT_ID}}",
  "items": [{ "ticketCategoryId": "{{TICKET_CATEGORY_ID}}", "quantity": 2 }],
  "promoCode": "{{PROMO_CODE}}"
}
```

Expected:
- HTTP `401 Unauthorized`

### Scenario G11 (Negative): Guest cannot do admin-only actions
Use Guest token and test:
1. `GET {{BASE_URL}}/api/analytics/dashboard/{{ORG_ID}}?startDate={{START_DATE}}&endDate={{END_DATE}}`
2. `POST {{BASE_URL}}/api/promo-codes?eventId={{EVENT_ID}}&code=SAVE10&type=PERCENTAGE&value=10&maxUses=10&validFrom=2026-04-01T00:00:00&validUntil=2026-05-01T23:59:59`
3. `POST {{BASE_URL}}/api/orders/{{ORDER_ID}}/refund`

Expected:
- HTTP `403 Forbidden` for endpoints guarded by `@PreAuthorize` (admin roles).
- If you see `401`, your `ACCESS_TOKEN` wasn’t set correctly.

---

## 5. Role: Scanner (SCANNER)

### Scenario S1: Login as Scanner
`POST {{BASE_URL}}/api/auth/login`
```json
{
  "email": "scanner@eventticketing.com",
  "password": "scanner123"
}
```

Copy `data.accessToken` into `ACCESS_TOKEN`

### Scenario S2: Validate ticket QR code
Endpoint:
`POST {{BASE_URL}}/api/tickets/validate`

Query params:
- `qrCode={{QR_CODE}}`
- `scannerId={{SCANNER_ID}}` (must be UUID)
- `scannerName=MainGate`

Expected:
- HTTP `200`
- `data.status` should become `VALIDATED`
- `data.validatedBy` / `data.scannerDeviceId` populated as per service logic

### Scenario S3: Verify ticket status by id
`GET {{BASE_URL}}/api/tickets/{{TICKET_ID}}`

Expected:
- HTTP `200`
- `data.status` reflects the validation

### Scenario S5: Scanner can also transfer/cancel (authenticated)
1. Transfer:
   - `POST {{BASE_URL}}/api/tickets/{{TICKET_ID}}/transfer`
   Body:
```json
{
  "recipientEmail": "friend@example.com",
  "message": "Transferring as scanner test"
}
```
2. Cancel:
   - `POST {{BASE_URL}}/api/tickets/{{TICKET_ID}}/cancel`

Expected:
- HTTP `200` for both if the ticket is in a transferable/cancellable state

### Scenario S4 (Negative): Scanner cannot access analytics/admin-only
1. `GET {{BASE_URL}}/api/analytics/event/{{EVENT_ID}}/sales?startDate={{START_DATE}}&endDate={{END_DATE}}`
2. `POST {{BASE_URL}}/api/promo-codes/{{PROMO_CODE_ID}}/deactivate`

Expected:
- HTTP `403 Forbidden`

---

## 6. Role: SUPER_ADMIN (SUPER_ADMIN)

### Scenario A1: Login as SUPER_ADMIN
`POST {{BASE_URL}}/api/auth/login`
```json
{
  "email": "admin@eventticketing.com",
  "password": "admin123"
}
```

Copy `data.accessToken` to `ACCESS_TOKEN`

### Scenario A2: User management (CRUD-ish via controller)
#### A2.1 Get all users (page)
`GET {{BASE_URL}}/api/users?page={{PAGE}}&size={{SIZE}}`
Expected: `ApiResponse<Page<UserDTO>>`

#### A2.2 Create user
Endpoint:
`POST {{BASE_URL}}/api/users`

Body (`CreateUserRequest`):
```json
{
  "email": "anotheruser@example.com",
  "keycloakId": "<keycloakId>",
  "firstName": "Another",
  "lastName": "User",
  "phone": "+21600000000",
  "role": "GUEST",
  "profileImageUrl": null
}
```
Expected: HTTP `201`, `data.id` is a UUID (set `USER_ID`)

#### A2.2.b Provision scanner/admin_org with credentials (SUPER_ADMIN only)
Endpoint:
`POST {{BASE_URL}}/api/users/provision`

Body:
```json
{
  "email": "newscanner@example.com",
  "password": "Scanner123!",
  "firstName": "New",
  "lastName": "Scanner",
  "phone": "+21600000000",
  "role": "SCANNER"
}
```
Expected:
- HTTP `201`
- Creates account in both Keycloak and `user_db`
- User can login immediately and change password later

Authorization rule:
- If request body has `"role": "ADMIN_ORG"`, caller must be `SUPER_ADMIN`.
- Non-superadmin callers should receive an authorization/business error.

#### A2.3 Update user profile
`PUT {{BASE_URL}}/api/users/{{USER_ID}}`

Body (`UpdateUserRequest`):
```json
{
  "firstName": "John",
  "lastName": "Updated",
  "phone": "+21611111111",
  "profileImageUrl": null
}
```
Expected: HTTP `200`

#### A2.4 Deactivate then Activate user
1. `POST {{BASE_URL}}/api/users/{{USER_ID}}/deactivate`
2. `POST {{BASE_URL}}/api/users/{{USER_ID}}/activate`

Expected: HTTP `200`, `ApiResponse<Void>`

#### A2.5 Permanently delete user (hard delete)
`DELETE {{BASE_URL}}/api/users/{{USER_ID}}/permanent`

Expected:
- HTTP `200`
- User is removed from both `user_db` and Keycloak realm

#### A2.6 Reset register-created users safely (keep SUPER_ADMIN)
`DELETE {{BASE_URL}}/api/users/reset/registered`

Expected:
- HTTP `200`
- Response includes deleted/skipped counts
- Only accounts created via public register flow are removed
- `SUPER_ADMIN` is excluded automatically

> Note: Some “self” checks in `UserController` compare the route `id` with JWT `sub`. In case “self” authorization fails, use admin role endpoints instead.

### Scenario A3: Organization management
1. `POST {{BASE_URL}}/api/organizations` (set required fields)
Body (`CreateOrganizationRequest`):
```json
{
  "name": "Tech Corp",
  "email": "org@techcorp.com",
  "phone": "+21600000000",
  "address": "123 Main St",
  "city": "Tunis",
  "country": "TN"
}
```
2. Capture `data.id` into `ORG_ID`

3. `PUT {{BASE_URL}}/api/organizations/{{ORG_ID}}`
Body (`UpdateOrganizationRequest`):
```json
{
  "name": "Tech Corp Updated",
  "email": "org@techcorp.com",
  "phone": "+21600000000",
  "address": "456 Second St",
  "city": "Tunis",
  "country": "TN",
  "domain": "techcorp.com"
}
```

### Scenario A4: Subscriptions (limit management)
1. `GET {{BASE_URL}}/api/subscriptions/organization/{{ORG_ID}}/can-create-event`
Expected: boolean
2. `POST {{BASE_URL}}/api/subscriptions/organization/{{ORG_ID}}/increment-event-count`
Expected: HTTP `200`

### Scenario A5: Event management
#### A5.1 Create event
Endpoint:
`POST {{BASE_URL}}/api/events`

Body (`CreateEventRequest`) — **start/end dates must be in the future**:
```json
{
  "organizationId": "{{ORG_ID}}",
  "name": "Tech Conference 2026",
  "description": "A major technology conference",
  "slug": "tech-conference-2026",
  "category": "CONFERENCE",
  "startDate": "2026-06-01T09:00:00",
  "endDate": "2026-06-03T18:00:00",
  "location": {
    "name": "Main Venue",
    "address": "1 Infinite Loop",
    "city": "Tunis",
    "state": "Tunis",
    "country": "TN",
    "zipCode": "1000",
    "latitude": 36.8065,
    "longitude": 10.1815,
    "mapsUrl": "https://maps.example.com"
  },
  "capacity": 5000,
  "bannerImageUrl": null,
  "thumbnailImageUrl": null,
  "ticketCategories": [
    {
      "name": "Standard",
      "description": "Standard seat",
      "price": 99.99,
      "quantity": 100,
      "saleStartDate": "2026-04-10T00:00:00",
      "saleEndDate": "2026-05-20T23:59:59",
      "color": "#000000",
      "features": ["Seat", "Access"]
    }
  ],
  "scheduleItems": [
    {
      "time": "09:30:00",
      "title": "Opening",
      "description": "Welcome",
      "speaker": "Tisqra Team",
      "location": "Room 1",
      "sortOrder": 1
    }
  ]
}
```
Expected: HTTP `201`, set `EVENT_ID` and `TICKET_CATEGORY_ID`

#### A5.2 Update event
`PUT {{BASE_URL}}/api/events/{{EVENT_ID}}`

Body: same as `CreateEventRequest`, but you can omit optional fields if backend accepts nulls.

Expected: HTTP `200`

#### A5.3 Publish + Cancel
1. `POST {{BASE_URL}}/api/events/{{EVENT_ID}}/publish`
2. `POST {{BASE_URL}}/api/events/{{EVENT_ID}}/cancel`

Expected: HTTP `200`

### Scenario A6: Promo codes
#### A6.1 Create promo code
`POST {{BASE_URL}}/api/promo-codes`
Query params:
- `eventId={{EVENT_ID}}`
- `code={{PROMO_CODE}}`
- `type=PERCENTAGE` (or `FIXED_AMOUNT`)
- `value=10`
- `maxUses=100` (optional)
- `validFrom=2026-04-01T00:00:00`
- `validUntil=2026-05-01T23:59:59`

Expected: HTTP `201`, set `PROMO_CODE_ID`

#### A6.2 Deactivate promo code
`POST {{BASE_URL}}/api/promo-codes/{{PROMO_CODE_ID}}/deactivate`

Expected: HTTP `200`

### Scenario A7: Orders + Payments refunds + verify
1. Create an order as Guest (see Section 4) to obtain `ORDER_ID`
2. Process payment as Guest to obtain `PAYMENT_ID` (see Section 4 happy path steps, then call process payment)

#### A7.1 Refund order (admin roles)
`POST {{BASE_URL}}/api/orders/{{ORDER_ID}}/refund`

Expected: HTTP `200`

#### A7.2 Refund payment
`POST {{BASE_URL}}/api/payments/{{PAYMENT_ID}}/refund`
Body (`RefundRequest`):
```json
{
  "amount": 10.00,
  "reason": "Customer requested"
}
```
Expected: HTTP `200`

#### A7.3 Verify payment
`GET {{BASE_URL}}/api/payments/verify/{{PAYMENT_PROVIDER_PAYMENT_ID}}`

Expected: HTTP `200` and `data` boolean

### Scenario A8: Analytics
1. `GET {{BASE_URL}}/api/analytics/dashboard/{{ORG_ID}}?startDate={{START_DATE}}&endDate={{END_DATE}}`
2. `GET {{BASE_URL}}/api/analytics/event/{{EVENT_ID}}/sales?startDate={{START_DATE}}&endDate={{END_DATE}}`

Expected:
- HTTP `200`
- `success: true`
- `data.totalRevenue`, `data.topEvents`, etc.

### Scenario A9 (Negative): SUPER_ADMIN cannot deactivate unless role matches
Try with `ADMIN_ORG` token (not super admin) for a SUPER_ADMIN-only endpoint:
1. Login as ADMIN_ORG
2. `POST {{BASE_URL}}/api/users/{{USER_ID}}/deactivate`

Expected:
- HTTP `403 Forbidden`

---

## 7. Role: ADMIN_ORG (ADMIN_ORG)

### Scenario O1: Login as ADMIN_ORG
`POST {{BASE_URL}}/api/auth/login`
```json
{
  "email": "organizer@eventticketing.com",
  "password": "organizer123"
}
```

Copy `data.accessToken` to `ACCESS_TOKEN`

### Scenario O2: Organization + Subscriptions
Repeat Scenario A3 and A4 (create/update organization + subscription checks).

Expected:
- HTTP `201` for create
- HTTP `200` for get/update/subscription endpoints

### Scenario O3: Event management
Repeat Scenario A5 (create/update/publish/cancel).

Expected:
- HTTP `201` create
- HTTP `200` publish/cancel

### Scenario O4: Promo codes
Repeat Scenario A6.

Expected:
- AdminOrg is allowed by `hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')`
- HTTP `201` create and HTTP `200` deactivate

### Scenario O5: Orders + Tickets workflow (end-to-end test)
1. Create event and capture `TICKET_CATEGORY_ID`
2. Login as Guest and execute order->payment->confirm->complete->tickets
3. (Optional) Login as Scanner and validate QR

You are mainly verifying that your event + ticket categories integrate with ordering and ticket generation.

### Scenario O6: Analytics
Repeat Scenario A8.

Expected: HTTP `200`

### Scenario O7 (Negative): ADMIN_ORG cannot deactivate/activate users
Use admin_org token:
1. `POST {{BASE_URL}}/api/users/{{USER_ID}}/deactivate`
2. `POST {{BASE_URL}}/api/users/{{USER_ID}}/activate`

Expected:
- HTTP `403 Forbidden` because controller uses `hasRole('SUPER_ADMIN')` only.

### Scenario O8 (Negative): ADMIN_ORG can’t call SUPER_ADMIN-only analytics/payment verify restrictions (if any exist)
Endpoints that are guarded by `hasAnyRole('SUPER_ADMIN','ADMIN_ORG')` should still pass for ADMIN_ORG.
Verify the "most sensitive" admin endpoint you expect to be forbidden:
- `GET {{BASE_URL}}/api/payments/verify/{{PAYMENT_PROVIDER_PAYMENT_ID}}`

If you got `403`, confirm it is due to missing/invalid token or role mapping; if you got `200`, that means ADMIN_ORG is allowed (as per controller).

---

## 8. Final Consistency Checks

Run these sanity checks after your happy-path flow:
1. `GET /api/events/{{EVENT_ID}}` returns the event with correct `categories` and schedule
2. `GET /api/orders/{{ORDER_ID}}` shows the expected status transitions after confirm/complete
3. `POST /api/tickets/generate` returns tickets whose `qrCode` matches what scanner validates
4. `GET /api/tickets/{{TICKET_ID}}` after scanner validation shows `status: VALIDATED`
5. Notifications show up for the guest user after order/ticket actions (depending on service implementation)

---

## 9. Notes / Known gotchas

1. **Dates for events**: `CreateEventRequest.startDate` is validated with `@Future`, so keep `startDate/endDate` in the future.
2. **Enums**: Use the enum string values exactly as Java declares them:
   - EventCategory: `CONFERENCE`, `WORKSHOP`, `SEMINAR`, ...
   - Promo DiscountType: `PERCENTAGE` or `FIXED_AMOUNT`
   - PaymentMethod: `CREDIT_CARD`, `DEBIT_CARD`, ...
3. **Ticket validation inputs**:
   - `scannerId` must be a UUID
   - `qrCode` must come from a generated ticket (`TicketDTO.qrCode`)
4. **Self-authorizations in UserController**:
   - Some endpoints compare route UUID `id` to JWT `sub`. If “self” operations behave unexpectedly, use admin calls with admin roles instead.

