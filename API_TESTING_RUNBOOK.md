# TISQRA API Testing Runbook (Full Coverage)

This runbook is the "single source of truth" for validating API correctness across all backend services behind the gateway.

Use it with:
- `POSTMAN_TESTING_GUIDE.md` (scenario-oriented walkthrough)
- `TISQRA_USER_FLOW_aligned.postman_collection.json` (executable requests)

## 1) Test Environment Baseline

- Start infra and services with `docker-compose.yml`.
- After backend code changes, rebuild and restart only the affected service before retesting:
  - `docker compose up -d --build <service-name>`
  - Example: `docker compose up -d --build user-service`
- Ensure these core endpoints are healthy:
  - `GET {{BASE_URL}}/actuator/health` -> `200`
  - `GET http://localhost:8761/actuator/health` -> `200`
  - `GET http://localhost:8888/actuator/health` -> `200`
- Verify Keycloak realm is loaded and seeded users can login.
- Use MailHog (`http://localhost:8025`) for verification and reset emails (SMTP sink in local Docker).
- After restarting auth-related services, run login again to get a fresh `ACCESS_TOKEN` before calling protected endpoints.

## 2) Required Postman Variables

Set before running:
- `BASE_URL`, `PAGE`, `SIZE`
- `EMAIL`, `ACCESS_TOKEN`, `USER_ID`, `VERIFY_TOKEN`
- `RESET_TOKEN`, `NEW_PASSWORD`
- `MANAGED_USER_EMAIL`, `MANAGED_USER_PASSWORD`
- `ORG_ID`, `EVENT_ID`, `TICKET_CATEGORY_ID`
- `PROMO_CODE`, `PROMO_CODE_ID`
- `ORDER_ID`, `ORDER_NUMBER`
- `PAYMENT_ID`, `PAYMENT_PROVIDER_PAYMENT_ID`
- `TICKET_ID`, `QR_CODE`, `SCANNER_ID`
- `NOTIFICATION_ID`, `START_DATE`, `END_DATE`

Email verification token format:
- Registration/resend now sends a **6-digit numeric code** via SMTP email.
- Put that code in `VERIFY_TOKEN` (or paste the full verify link; backend normalizes either).

## 3) Execution Order (Critical)

Run in this order to avoid false negatives:
1. Auth register/verify/login/logout/reset
2. Organization create/update + subscription checks
3. Event create/update/publish
4. Ticket category get/reserve/release
5. Promo code create/validate/list/deactivate
6. Order create/confirm/complete/cancel/refund
7. Payment process/get/get-by-order/refund/verify
8. Ticket generate/get/list/validate/transfer/cancel
9. Notification list/read/delete
10. Analytics dashboard/event-sales

## 4) Assertions Per Endpoint Category

For each request verify:
- Status code is expected (`2xx`, `4xx`, or `5xx` for negative testing only)
- `success` flag and `error` shape are consistent
- Response data schema is stable (id fields, enums, timestamps)
- Side effects are persisted (DB row changes, status transitions)
- Downstream effects are visible when expected (notifications, analytics)
- Auth payload includes `data.user`, `data.access_token`, and `data.token_type`.

## 5) Negative Test Matrix

Run each negative test at least once:
- Missing token on protected endpoints -> `401`
- Wrong role on admin endpoints -> `403`
- Unknown resource id -> `404`
- Invalid payload shape/enum/date -> `400`
- Duplicate unique fields (email, slug, promo code) -> `409`/`400` based on service behavior

Examples:
- Guest token calling `POST /api/events`
- Invalid UUID in path for id-based endpoints
- Event creation with past `startDate`
- Promo validation with invalid `eventId` + unknown `code`

## 6) Cross-Service Consistency Checks

After happy path:
- Confirmed/completed order matches payment state.
- Generated tickets map to order items and quantities.
- Ticket validation updates ticket status and scanner metadata.
- Notification entries exist for key actions (if event-driven path is configured).
- Analytics reports reflect created and paid orders.

## 7) Deletion and Data Integrity Checks

When deleting entities, verify:
- Data is removed (or soft-deleted) from the owning service DB.
- Linked read models/caches are invalidated.
- Dependent services process delete events (if asynchronous).
- No stale records remain in Redis.

Minimum checks after delete:
- API GET by id returns `404` (or inactive status for soft delete).
- Related list endpoints no longer include the deleted entity.
- Keycloak user deletion is validated separately for identity lifecycle.
- `DELETE /api/users/reset/registered` keeps `SUPER_ADMIN` and returns deleted/skipped counts.

## 8) DB Validation Checklist (Postgres + Keycloak)

Because identity has dual persistence (app DB + Keycloak), always validate both:
- App user row in `user_db`
- Keycloak user in realm `event-ticketing`

When "delete user everywhere" is required:
1. Delete user in `user-service` domain.
2. Delete or disable user in Keycloak admin API.
3. Publish `user.deleted` integration event.
4. Consumer services perform cleanup (notifications, tickets, analytics references).

## 9) Exit Criteria (Definition of Done)

Backend is considered stable when:
- All happy-path requests in collection pass.
- Negative matrix passes with expected 4xx codes.
- No unexpected 500s in logs during full run.
- Deletion checks pass in API + DB + identity provider.
- Gateway routes and auth behavior match documentation.



%
{
  "email": "admin@eventticketing.com",
  "password": "admin123"
}
%
