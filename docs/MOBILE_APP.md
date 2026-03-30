# Tisqra Mobile (Flutter)

This folder contains a clean, scalable Flutter mobile app (Android + iOS) that integrates with the backend microservices in this repository via the API Gateway using JWT authentication (Keycloak).

## Project Location

- Flutter project: `mobile/`

## Tech Stack (Mobile)

- Flutter + Material 3
- `flutter_riverpod` (state management)
- `go_router` (navigation)
- `dio` (HTTP client)
- `flutter_secure_storage` (token storage)
- `qr_flutter` (ticket QR rendering)
- `share_plus` (share QR/ticket)

## Repository / Folder Structure

- `mobile/lib/core/`:
  - API client, environment configuration, auth state/controller, routing shell/theme
- `mobile/lib/shared/`:
  - reusable widgets (buttons, inputs)
- `mobile/lib/features/`:
  - `auth/`, `home/`, `orders/`, `tickets/`, `notifications/`, `profile/`, `payments/`

## Backend Connectivity (API Gateway)

All API calls go through:

- `API_BASE_URL` (default varies by platform, see below)

### Important Auth Flow

1. User logs in via:
   - `POST /api/auth/login` (gateway -> `user-service`)
2. Login response contains:
   - `accessToken` + `refreshToken`
3. The app decodes the JWT `sub` claim and maps it to your platform user UUID:
   - `GET /api/users/keycloak/{keycloakSub}`
4. The app stores tokens + `userId` in secure storage and uses `Authorization: Bearer <accessToken>` for all protected calls.

## Screens ↔ Backend Endpoints

### Authentication

- Login: `POST /api/auth/login`
- Register: `POST /api/auth/register`
- Email verification: `POST /api/auth/email/verify?token=...`
- Forgot password: `POST /api/auth/password/reset-request?email=...`
- Reset password: `POST /api/auth/password/reset` (body: `token`, `newPassword`)

### Home / Dashboard

- Active order count + order tiles: `GET /api/orders/user/{userId}`
- Ticket count + ticket tiles: `GET /api/tickets/user/{userId}`
- Notification count + recent items: `GET /api/notifications/user/{userId}`

### Orders

- Orders list: `GET /api/orders/user/{userId}`
- Order details: `GET /api/orders/{orderId}`
- Cancel order: `POST /api/orders/{orderId}/cancel`
- Payment is handled from the **Payment process** flow (see Payments).

### Tickets

- Tickets list: `GET /api/tickets/user/{userId}`
- Ticket details: `GET /api/tickets/{ticketId}`

### Notifications

- List: `GET /api/notifications/user/{userId}`
- Mark as read: `POST /api/notifications/{notificationId}/read`
- Delete (swipe): `DELETE /api/notifications/{notificationId}`

> Note: your repo previously had no REST controller in `notification-service`. To support this screen, a minimal Notifications REST controller was added in `services/notification-service`.

### Payments

- Saved cards are stored locally (demo) using `shared_preferences`.
- Payment processing:
  - `POST /api/payments/process` with `orderId` + card details
- Payment success/failure screens are shown based on the response.

## Local Setup

### 1) Run Backend

Start your infrastructure and services (Docker Compose / Keycloak / Kafka / Postgres).

The app assumes the API Gateway is reachable at:

- Android emulator: `http://10.0.2.2:8080`
- iOS simulator/device: `http://localhost:8080`

### 2) Environment Variables

The app reads these at build time:

- `API_BASE_URL` (optional)
- `KEYCLOAK_BASE_URL` (default: `http://localhost:8180`)
- `KEYCLOAK_REALM` (default: `tisqra`)
- `KEYCLOAK_CLIENT_ID` (default: `event-ticketing-client`)
- `KEYCLOAK_CLIENT_SECRET` (default: empty)

### 3) Run the App

From `mobile/`:

```bash
flutter run
```

### Android Emulator Tip

If your backend is running on your host machine and you use an emulator:

- keep `API_BASE_URL` default (`10.0.2.2:8080`)

## Testing

```bash
flutter test
```

## Known Limitations / Notes

- Profile editing calls `PUT /api/users/{id}`. If Keycloak principal mapping does not match your `@PreAuthorize` logic, the update may be rejected (UI still exists and shows error messages).
- Resend email verification is currently a UI placeholder (no resend endpoint in the provided backend).

