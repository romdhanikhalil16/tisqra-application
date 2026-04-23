# TISQRA Mobile Application

This is the Flutter mobile application for the TISQRA Event Ticketing Platform.

## Role-Based Features

The app supports multiple roles to manage and participate in events.

### Guest (GUEST)
- Browse upcoming events and search by category.
- Purchase tickets and apply promo codes.
- View and manage purchased tickets (transfer/cancel).
- Receive notifications regarding orders and tickets.

### Scanner (SCANNER)
- Dedicated Scanner Dashboard.
- Scan ticket QR codes using the device camera (`mobile_scanner`).
- Validate tickets in real-time at the event gateway.

### Organization Admin (ADMIN_ORG)
- Manage organization details.
- View and create new events.
- Manage organization users (create, activate, deactivate, delete).
- Provision new Scanner and Guest accounts.

### Super Admin (SUPER_ADMIN)
- System-wide overview and notifications.
- Manage all platform organizations.
- Provision Organization Admins and Scanners.
- Access global analytics and administrative actions.

## Architecture & State Management
- **State Management**: Riverpod (`flutter_riverpod`)
- **Routing**: GoRouter (`go_router`) with role-based redirection logic.
- **Networking**: Dio (`dio`) with comprehensive error handling and `ApiResponse` wrappers.

## Getting Started
Ensure you have the backend services running via Docker.
1. Create a `.env` file referencing the API Gateway.
2. Run `flutter pub get`.
3. Run `flutter run` on your preferred device.
