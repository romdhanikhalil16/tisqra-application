# Setup Guide

## Prerequisites

## System tools
- Java 17+
- Maven 3.9+
- Docker + Docker Compose (recommended for PostgreSQL and containerized backend)
- Flutter SDK (stable channel)
- Android Studio / Xcode tooling (depending on target platform)

## Accounts/services
- SMTP credentials (required by backend config)
- Firebase config files for mobile:
  - `vms-fe/android/app/google-services.json`
  - `vms-fe/ios/Runner/GoogleService-Info.plist`

## 1) Run backend (`vms`)

Backend location:
- `vms/`

### Environment variables (minimum)
- `DB_HOST` (default `postgresql` in Docker network)
- `DB_PORT` (default `5432`)
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SMTP_SERVER_HOST`
- `SMTP_SERVER_PORT`
- `SMTP_SERVER_USERNAME`
- `SMTP_SERVER_PASSWORD`
- `MAIL_FROM`

Optional but important:
- `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`
- `INIT_FOLDER_PATH` (for admin/offices seed JSON)
- `VMS_PORT`

### Option A: Docker Compose (recommended)

From `vms/`:
- `docker compose up --build`

This starts:
- PostgreSQL container
- Spring Boot backend container

Default API base:
- `http://localhost:<VMS_PORT>/vms`

### Option B: Run locally with Maven

1. Ensure PostgreSQL is running and accessible.
2. Export required env vars.
3. From `vms/`, run:
   - `mvn spring-boot:run`

## 2) Run mobile app (`vms-fe`)

Mobile location:
- `vms-fe/`

### Install dependencies
- `flutter pub get`

### Configure backend URL
Use Dart define so app points to your backend context path:
- `--dart-define=BASE_URL=http://<host>:<port>/vms`

Example:
- `flutter run --dart-define=BASE_URL=http://10.0.2.2:8080/vms`

Notes:
- Android emulator uses `10.0.2.2` for host machine localhost.
- iOS simulator can usually use `localhost`.

### Run
- `flutter run`

## 3) Verify setup

- Backend health:
  - call one public endpoint, e.g. `GET /vms/users/{email}` (returns type or not found)
- Auth:
  - call `POST /vms/employees/token` with Basic auth to receive JWT
- Mobile:
  - ensure login and meeting list load without network/config errors

## 4) Configuration and secrets notes

- Do not commit production secrets (DB passwords, SMTP credentials, JWT private key, Firebase service account JSON).
- Keep environment-specific values outside code using env vars and `--dart-define`.
- Ensure backend context path `/vms` matches the mobile `BASE_URL`.
