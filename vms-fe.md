# vms-fe (Flutter) - Architecture and Integration

## Mobile overview

`vms-fe` is a Flutter application with staff-facing and visitor-facing flows for meeting invitations, QR generation, QR scanning, and check-in.

Primary entry point:
- `vms-fe/lib/main.dart`

The app initializes Firebase, local storage (Hive), secure storage, and runs `MaterialApp` with `SplashScreen`.

## Project structure

- `vms-fe/lib/screens`: UI screens
- `vms-fe/lib/controllers`: controller classes coordinating UI actions + service calls
- `vms-fe/lib/services`: HTTP integrations and auth/token flows
- `vms-fe/lib/models`: data models and JSON mapping
- `vms-fe/lib/jwt`: JWT helpers + auth interceptor + token holder
- `vms-fe/lib/visitorpart/lib`: parallel visitor module (its own screens/controllers/services/models/jwt)
- `vms-fe/assets`: app images and QR placeholders

## State and navigation

- **State management**: mostly local widget state plus `Provider` with `AppState` (`ChangeNotifier`).
- **Navigation**: imperative `Navigator.push` / `pushReplacement`; no centralized router table.
- **Startup flow**:
  - `SplashScreen` -> `AnimatedStartScreen` -> authentication/check-in path.

## API integration design

Staff API client:
- `vms-fe/lib/global_vars.dart`
  - `baseUrl` from `--dart-define=BASE_URL=...` (default points to `/vms`)
  - Shared `Dio` client with `AuthInterceptor` for Bearer token.

Visitor API client:
- `vms-fe/lib/visitorpart/lib/visitor_vars.dart`
  - Separate `Dio` + visitor auth interceptor + token holder.

Auth handling:
- Staff login service: `vms-fe/lib/services/auth_service.dart`
- Token management: `vms-fe/lib/jwt/token_holder.dart`
- Interceptor adds `Authorization: Bearer <token>` except token endpoints.

## Screen and module responsibilities

- **Authentication screens**
  - `lib/screens/authentication/*`
  - Login, forgot/reset password, verification.
- **Meeting module**
  - Screens: `lib/screens/meeting/*`
  - Controllers: `lib/controllers/meetings/*`
  - Services: `lib/services/meeting_service.dart`, `participant_service.dart`
- **Entity management modules**
  - Companies, departments, domains, employees, offices, visitors.
  - Organized as screen + controller + service + model groups.
- **Notification/profile utilities**
  - Notification screens and banners, profile/settings, connectivity fallback screens.

## QR flows (critical business path)

### QR generation
- Trigger points in meeting details UI:
  - `lib/screens/meeting/meeting_details_bottom.dart`
  - helper checks in `lib/screens/utils/meeting_utils.dart`
- QR payload is generated using participant/meeting context and signed via JWT helper:
  - `lib/widgets/qr_popup.dart`
  - requests secret from backend endpoint `/participants/{meetingId}/secret`

### QR scanning and validation
- Scanner UI:
  - `lib/screens/meeting/meeting_scanner.dart`
- Validation logic:
  - `lib/controllers/meetings/MeetingScannerController.dart`
  - decodes token, validates participant and signature, emits `MeetingScanResult`
- Result UI:
  - `lib/screens/meeting/meeting_access_result_scan.dart`
  - `lib/screens/meeting/meeting_pass_card.dart`

### Visitor check-in path
- Visitor check-in and QR scan screens under:
  - `lib/visitorpart/lib/screens/checkin/*`
- Redeem/check-in API usage maps to backend public endpoints and subsequent authenticated visitor requests.

## Important integration points with backend

- Backend context path expected by mobile API clients: `/vms`
- Token endpoints used by app:
  - `/employees/token` (staff)
  - `/visitors/token` (visitor)
- Meeting/participant QR endpoints used heavily:
  - `/meetings/*`
  - `/participants/{meetingId}/secret`
  - `/meetings/{id}/resolve`

## Gaps and ambiguous areas

- Some services use hardcoded non-`/vms` URLs (`company_service`, `domain_service`, `department_service`, `forgot_password_service`) while core modules use `global_vars.dart`; this may cause environment inconsistency.
- Visitor default base URL in `visitor_vars.dart` appears malformed (`https:/...` single slash).
- Navigation is distributed across many screens with no central route map, making audit and guard policy harder.
