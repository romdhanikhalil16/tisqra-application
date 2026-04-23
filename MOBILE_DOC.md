# Mobile Doc

This document is the source of truth for the mobile application under `mobile/`.
Whenever a screen or route is added/removed/renamed, this file must be updated in the same change.

## Auth And Session

- Auth controller: `mobile/lib/core/auth/auth_controller.dart`
- Auth state: `mobile/lib/core/auth/auth_state.dart`
- Router: `mobile/lib/core/routing/app_router.dart`
- Token persistence: `mobile/lib/core/auth/token_storage.dart`

## Route Map

Public routes:

- `/` -> `SplashScreen` (`mobile/lib/features/auth/presentation/splash_screen.dart`)
- `/login` -> `LoginScreen` (`mobile/lib/features/auth/presentation/login_screen.dart`)
- `/register` -> `RegisterScreen` (`mobile/lib/features/auth/presentation/register_screen.dart`)
- `/verify` -> `VerifyEmailScreen` (`mobile/lib/features/auth/presentation/verify_email_screen.dart`)
- `/forgot-password` -> `ForgotPasswordScreen` (`mobile/lib/features/auth/presentation/forgot_password_screen.dart`)
- `/reset-password` -> `ResetPasswordScreen` (`mobile/lib/features/auth/presentation/reset_password_screen.dart`)
- `/unauthorized` -> `UnauthorizedScreen` (`mobile/lib/features/auth/presentation/unauthorized_screen.dart`)

Protected app-shell routes:

- `/app/home` -> `HomeScreen`
- `/app/orders` -> `OrdersScreen`
- `/app/orders/:orderId` -> `OrderDetailsScreen`
- `/app/tickets` -> `TicketsScreen`
- `/app/tickets/:ticketId` -> `TicketDetailsScreen`
- `/app/notifications` -> `NotificationsScreen`
- `/app/profile` -> `ProfileScreen`
- `/app/profile/edit` -> `EditProfileScreen`
- `/app/profile/settings` -> `SettingsScreen`
- `/app/payments` -> `PaymentsScreen`
- `/app/payments/add` -> `AddPaymentScreen`
- `/app/payments/process/:orderId` -> `PaymentProcessScreen`
- `/app/payments/success/:orderId` -> `PaymentSuccessScreen`
- `/app/payments/failure/:orderId` -> `PaymentFailureScreen`

Protected admin routes:

- `/super-admin` -> `SuperAdminDashboard`
- `/super-admin/org-admins` -> `ListOrgAdminsScreen`
- `/super-admin/create-org-admin` -> `CreateOrgAdminScreen`
- `/admin-org` -> `AdminOrgDashboard`
- `/admin-org/users` -> `ListOrgUsersScreen`
- `/admin-org/create-user` -> `CreateRegularUserScreen`

## Existing Screen Files

Auth:

- `mobile/lib/features/auth/presentation/forgot_password_screen.dart`
- `mobile/lib/features/auth/presentation/login_screen.dart`
- `mobile/lib/features/auth/presentation/register_screen.dart`
- `mobile/lib/features/auth/presentation/reset_password_screen.dart`
- `mobile/lib/features/auth/presentation/splash_screen.dart`
- `mobile/lib/features/auth/presentation/unauthorized_screen.dart`
- `mobile/lib/features/auth/presentation/verify_email_screen.dart`

Home/Main:

- `mobile/lib/features/home/presentation/home_screen.dart`
- `mobile/lib/features/main/presentation/app_shell.dart`

Orders:

- `mobile/lib/features/orders/presentation/orders_screen.dart`
- `mobile/lib/features/orders/presentation/order_details_screen.dart`

Tickets:

- `mobile/lib/features/tickets/presentation/tickets_screen.dart`
- `mobile/lib/features/tickets/presentation/ticket_details_screen.dart`

Payments:

- `mobile/lib/features/payments/presentation/payments_screen.dart`
- `mobile/lib/features/payments/presentation/add_payment_screen.dart`
- `mobile/lib/features/payments/presentation/payment_process_screen.dart`
- `mobile/lib/features/payments/presentation/payment_success_screen.dart`
- `mobile/lib/features/payments/presentation/payment_failure_screen.dart`

Profile:

- `mobile/lib/features/profile/presentation/profile_screen.dart`
- `mobile/lib/features/profile/presentation/edit_profile_screen.dart`
- `mobile/lib/features/profile/presentation/settings_screen.dart`

Notifications:

- `mobile/lib/features/notifications/presentation/notifications_screen.dart`

Admin:

- `mobile/lib/features/admin/presentation/super_admin_dashboard.dart`
- `mobile/lib/features/admin/presentation/list_org_admins_screen.dart`
- `mobile/lib/features/admin/presentation/create_org_admin_screen.dart`
- `mobile/lib/features/admin/presentation/admin_org_dashboard.dart`
- `mobile/lib/features/admin/presentation/list_org_users_screen.dart`
- `mobile/lib/features/admin/presentation/create_regular_user_screen.dart`

## Guard Rules

- Protected routes require `isAuthenticated && !isExpired`.
- `SCANNER` is blocked from regular mobile app routes.
- `SUPER_ADMIN` and `ADMIN_ORG` can log in and are routed to their respective dashboards.
- Non-admin users attempting to access `/super-admin/*` or `/admin-org/*` are redirected to `/unauthorized`.
- Authenticated users are redirected away from auth pages to their respective dashboards or `/app/home`.

## Deep Link And Query Parameters

The app supports passing context via query parameters (useful for email verification flows):

- `/verify?email=<email>&token=<code-or-link>`: pre-fills token and auto-verifies.
- `/login?email=<email>`: pre-fills login email.

