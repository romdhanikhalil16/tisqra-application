# End-to-End Workflow

## 1) Creating invitation (meeting creation)

1. Staff user logs in through `POST /vms/employees/token` (Basic auth) and receives JWT.
2. Mobile app stores JWT and uses it in Bearer auth for protected APIs.
3. Staff creates a meeting through `POST /vms/meetings` with organizer/proxy, visitors/guests, time window, and office.
4. Backend validates participants, office assignment, schedule constraints, and persists meeting with status lifecycle fields.

## 2) Generating QR code

1. In mobile meeting details, eligible participant triggers QR generation.
2. Mobile requests participant secret from `GET /vms/participants/{meetingId}/secret`.
3. Backend verifies participant membership and meeting access, then returns secret payload.
4. Mobile signs a QR token payload (`mid`, `oid`, `pid`, `type`, `exp`) and renders QR image for entry.

## 3) Scanning and validation

1. Security/staff scanner opens QR scanner screen.
2. App decodes scanned token and validates token shape and expiry locally.
3. App calls backend resolve/check endpoints (notably `GET /vms/meetings/{id}/resolve?participant=...`) to verify participant-meeting relation and effective secret.
4. App verifies signature integrity and displays pass/fail result with context (meeting state, participant identity, office).

## 4) Backend processing after scan

1. Backend receives resolve/meeting state requests.
2. Meeting service checks:
   - meeting existence and lifecycle state
   - participant association and role
   - timing constraints and state transition rules
3. Backend returns meeting resolve data (status, participant/organizer details, secret when applicable).
4. Mobile decides final UI outcome:
   - valid pass -> access screen/card
   - invalid or not-yet-valid -> rejection/wait screen

## 5) Visitor onboarding/check-in flow (public endpoints)

1. Visitor pre-check-in can call `POST /vms/visitors/checkin`.
2. Visitor can redeem invitation/code via:
   - `GET /vms/visitors/redeem?code=...&visitor=...`
   - and deep links (`/vms/links/defer/{code}`, `/vms/links/redeem/{code}`)
3. Visitor login token flow uses `POST /vms/visitors/token` with Basic auth (TOTP code).
4. Visitor then performs authenticated operations using Bearer JWT.
