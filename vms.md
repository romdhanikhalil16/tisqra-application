# vms (Spring Boot) - Architecture and API

## Backend overview

`vms` is a Spring Boot 3 backend for meeting invitation and visitor access management.
It exposes REST APIs under the context path `/vms`, stores data in PostgreSQL, and uses JWT + role-based authorization (`admin`, `employee`, `visitor`).

Main backend entry point:
- `vms/src/main/java/com/zum/vms/VmsApplication.java`

Core runtime configuration:
- `vms/src/main/resources/application.properties`
- `vms/src/main/resources/vms-config.xml`
- `vms/src/main/resources/db/changelog/` (Liquibase)

## Architecture layers

- **Web/API layer**: `vms/src/main/java/com/zum/vms/web`
  - REST controllers for meetings, employees, visitors, offices, participants, users, and links.
- **Service layer**: `vms/src/main/java/com/zum/vms/service`
  - Business logic, lifecycle transitions, invitation validation, notifications.
- **Persistence layer**: `vms/src/main/java/com/zum/vms/repository`
  - Spring Data JPA + Querydsl repositories.
- **Domain and DTOs**:
  - Entities: `vms/src/main/java/com/zum/vms/model/domain`
  - API DTOs: `vms/src/main/java/com/zum/vms/model/dto`
- **Security**:
  - XML-based Spring Security config in `vms-config.xml`
  - JWT issuing/verification helpers in `util` and auth providers in `security`.
- **Initialization**:
  - Startup data initializers in `vms/src/main/java/com/zum/vms/init`.

## Key modules and responsibilities

- **Meeting module**
  - Controller: `MeetingWebController`
  - Service: `MeetingService`
  - Responsibilities: CRUD, state transitions (accept/pass/start/cancel), resolve for scanners, participant checks, secret handling, pending/expiry scheduled processing.
- **Employee module**
  - Controller: `EmployeeWebController`
  - Service: `EmployeeService`
  - Responsibilities: employee CRUD, profile updates, credential updates, JWT login (Basic -> JWT).
- **Visitor module**
  - Controller: `VisitorWebController`
  - Service: `VisitorService`
  - Responsibilities: visitor CRUD, check-in, invite redeem, TOTP/JWT login, profile picture upload.
- **Office module**
  - Controller: `OfficeWebController`
  - Service: `OfficeService`
  - Responsibilities: office CRUD, manager/default-office relationships.
- **Participant and deep-link support**
  - Controllers: `ParticipantWebController`, `DeepLinkingWebService`
  - Responsibilities: provide participant secret for QR generation; defer/redeem invitation links.

## Database and migrations

- **Database**: PostgreSQL (runtime), H2 (tests).
- **Migration engine**: Liquibase (`db.changelog-master.xml` includes changesets).
- **Important schema domains**:
  - `profiles` (user inheritance for employees/visitors)
  - `meeting` + participant/visitor mapping tables
  - `meeting_confirmation`
  - `office`
  - `token`
- **Startup initialization**:
  - Reads admin and offices seed files from `vms.init.folder.path` (default `/opt/init`).

## Security and authentication model

- **Token issuance**
  - `POST /vms/employees/token`: HTTP Basic (`email:password`) -> JWT
  - `POST /vms/visitors/token`: HTTP Basic (`email:totpCode`) -> JWT
- **Protected APIs**
  - Bearer JWT required: `Authorization: Bearer <token>`
  - Authorities come from token scope and are enforced by `vms-config.xml` URL rules.
- **Public APIs**
  - `/vms/users/{userId}`
  - `/vms/visitors/checkin`
  - `/vms/visitors/redeem`
  - `/vms/links/**`

## API catalog (by controller)

### Meetings (`/vms/meetings`)
- `GET /meetings`
- `GET /meetings/{id}`
- `GET /meetings/{id}/resolve?participant={email}`
- `POST /meetings`
- `PUT /meetings/{id}`
- `PATCH /meetings/{id}`
- `DELETE /meetings/{id}`
- `DELETE /meetings` (body: list of ids)
- `GET /meetings/{id}/accept`
- `GET /meetings/{id}/pass`
- `GET /meetings/{id}/start`
- `GET /meetings/{id}/cancel?details=...`

Request DTO (`MeetingDto`) includes:
- `id`, `startTime`, `endTime`, `autoStart`, `topic`, `status`, `details`
- `organizer`, `proxy`, `guests[]`, `visitors[]`, `office`, `confirmation`

### Employees (`/vms/employees`)
- `GET /employees`
- `GET /employees/{id}`
- `POST /employees`
- `PUT /employees/{id}` (currently unsupported in implementation)
- `PATCH /employees/{id}`
- `POST /employees/credentials?password=...`
- `POST /employees/picture` (multipart form)
- `DELETE /employees/{id}`
- `DELETE /employees` (body: list of ids)
- `POST /employees/token` (Basic auth, returns JWT)
- `GET /employees/resolve?term=...`

### Visitors (`/vms/visitors`)
- `GET /visitors`
- `GET /visitors/{id}`
- `POST /visitors`
- `PUT /visitors/{id}`
- `PATCH /visitors/{id}`
- `DELETE /visitors/{id}`
- `DELETE /visitors` (body: list of ids)
- `POST /visitors/picture` (multipart form)
- `POST /visitors/checkin` (public)
- `GET /visitors/redeem?code=...&visitor=...` (public)
- `POST /visitors/token` (Basic auth, returns JWT)

### Offices (`/vms/offices`)
- `GET /offices`
- `GET /offices/{id}`
- `POST /offices`
- `PUT /offices/{id}`
- `PATCH /offices/{id}`
- `DELETE /offices/{id}`
- `DELETE /offices` (body: list of ids)

### Participants (`/vms/participants`)
- `GET /participants/{meetingId}/secret`

### Users (`/vms/users`)
- `GET /users/{userId}` (returns account type)

### Links (`/vms/links`)
- `GET /links/defer/{code}`
- `GET /links/redeem/{code}`

## Notes and unclear points

- `PUT /employees/{id}` throws `UnsupportedOperationException` in current code.
- Security patterns in XML include placeholders like `/employees/{id}` that may be ambiguous in XML URL matcher semantics.
- Sensitive key files exist under `vms/src/main/resources/keys`; ensure secrets are managed safely in real deployments.
