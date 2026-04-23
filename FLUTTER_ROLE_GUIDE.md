# Flutter Role-Based Navigation Guide

This guide documents the role-based screens and navigation flows within the TISQRA Flutter mobile application.

## 1. Authentication Flow
- **All Users**: Start at `/login` or `/register` (public).
- **Redirection Logic**: Handled by `app_router.dart`. After login, users are dynamically redirected based on their Keycloak/backend `role`:
  - `GUEST` -> `/app/home`
  - `SCANNER` -> `/scanner`
  - `ADMIN_ORG` -> `/admin-org`
  - `SUPER_ADMIN` -> `/super-admin`

## 2. Role: Scanner (SCANNER)
**Primary Path**: `/scanner`
- **Scanner Dashboard** (`/scanner`): Displays the scanner's assigned info and a primary action button to open the scanner.
- **QR Scanner** (`/scanner/scan`): 
  - Utilizes `mobile_scanner` to access the device's camera.
  - Automatically captures QR codes and calls `POST /api/tickets/validate`.
  - Provides visual feedback (Snackbar) on success or failure of ticket validation.

## 3. Role: Organization Admin (ADMIN_ORG)
**Primary Path**: `/admin-org`
- **Admin Dashboard** (`/admin-org`): High-level overview of the organization.
- **User Management** (`/admin-org/users`):
  - Lists all users within the organization.
  - Swipe/Menu actions to **Activate**, **Deactivate**, and **Delete** accounts.
- **Create User** (`/admin-org/create-user`):
  - Form to provision new users.
  - Role dropdown allows creating either `GUEST` or `SCANNER` accounts assigned to the organization.
- **Event Management** (`/admin-org/events`):
  - Lists the organization's events.
  - Includes functionality to create new events (calling `POST /api/events`).

## 4. Role: Super Admin (SUPER_ADMIN)
**Primary Path**: `/super-admin`
- **Super Admin Dashboard** (`/super-admin`): Central control panel.
- **Manage Organizations** (`/super-admin/organizations`):
  - Lists all platform organizations.
  - Monitors status (Active/Inactive) and provides actions to toggle states.
- **Provision Admins** (`/super-admin/create-org-admin`):
  - Dedicated flow to create `ADMIN_ORG` accounts with full privileges.
- **Global User Management** (`/super-admin/org-admins`):
  - View and manage high-level administrators across the platform.

## 5. Role: Guest (GUEST)
**Primary Path**: `/app` (ShellRoute)
- **Home/Events** (`/app/home`): Browse public events.
- **Orders** (`/app/orders`): Manage purchases.
- **Tickets** (`/app/tickets`): View generated QR codes for entry.
- **Payments** (`/app/payments`): Payment processing flows.
- Guests are restricted from accessing any `/admin-org`, `/super-admin`, or `/scanner` paths.
