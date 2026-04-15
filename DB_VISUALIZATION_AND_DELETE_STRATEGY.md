# DB Visualization and Safe Deletion Strategy

## 1) Recommended DB Visualization Setup

To avoid pgAdmin "wrong instance/port" confusion, use the built-in Dockerized Adminer:
- URL: `http://localhost:8082`
- System: `PostgreSQL`
- Server: `postgres` (from inside Docker network) or `localhost:5433` (from host tools)
- Username: `postgres`
- Password: `root` (or your `.env` override)

Why this is safer:
- Adminer runs in the same Docker network as services.
- You are less likely to connect to a different local Postgres by mistake.
- All service databases (`user_db`, `organization_db`, ..., `keycloak_db`) are visible from one place.

## 2) What "Delete Everywhere" Means in Microservices

In this architecture, one delete can affect:
- Primary service DB row (owner of the entity)
- Keycloak identity (for users)
- Redis cache entries
- Other services holding references or denormalized copies

Direct cross-DB delete operations are not recommended.
Use service-owned deletion + integration events.

## 3) Best-Practice Deletion Flow

1. **Delete in owner service** within a local transaction.
2. **Emit a Kafka domain event** (example: `user.deleted`, `event.deleted`).
3. **Consumers** in other services remove dependent data or mark it inactive.
4. **Invalidate Redis keys** related to deleted entity and query lists.
5. **For users**, call Keycloak Admin API to delete/disable identity.
6. **Audit log** deletion request and completion status.

## 4) Hard Delete vs Soft Delete

- Use **soft delete** for critical business entities (users, orders, tickets) unless legal requirements require hard deletion.
- Use **hard delete** for short-lived technical entities where history is not required.

Suggested pattern:
- Add `deletedAt`, `deletedBy`, `isDeleted`
- Filter deleted rows from queries
- Keep asynchronous cleanup for linked data in other services

## 5) Safety Checklist Before Deleting

- Confirm target tenant/org context.
- Confirm no active business workflow depends on this entity.
- Snapshot key fields for audit.
- Verify cascade behavior in owner DB (if enabled).
- Verify Keycloak cleanup for user lifecycle.

## 6) Verification Checklist After Deleting

- API `GET by id` returns `404` or inactive state.
- Entity does not appear in list/search endpoints.
- Redis does not serve stale data.
- Consumer services processed delete events.
- Keycloak user is removed/disabled when applicable.

