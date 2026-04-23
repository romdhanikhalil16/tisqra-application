import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/core/auth/auth_state.dart';
import 'package:mobile/features/auth/presentation/forgot_password_screen.dart';
import 'package:mobile/features/auth/presentation/login_screen.dart';
import 'package:mobile/features/auth/presentation/register_screen.dart';
import 'package:mobile/features/auth/presentation/reset_password_screen.dart';
import 'package:mobile/features/auth/presentation/splash_screen.dart';
import 'package:mobile/features/auth/presentation/unauthorized_screen.dart';
import 'package:mobile/features/auth/presentation/verify_email_screen.dart';
import 'package:mobile/features/home/presentation/home_screen.dart';
import 'package:mobile/features/main/presentation/app_shell.dart';
import 'package:mobile/features/notifications/presentation/notifications_screen.dart';
import 'package:mobile/features/orders/presentation/order_details_screen.dart';
import 'package:mobile/features/orders/presentation/orders_screen.dart';
import 'package:mobile/features/payments/presentation/payments_screen.dart';
import 'package:mobile/features/payments/presentation/add_payment_screen.dart';
import 'package:mobile/features/payments/presentation/payment_process_screen.dart';
import 'package:mobile/features/payments/presentation/payment_success_screen.dart';
import 'package:mobile/features/payments/presentation/payment_failure_screen.dart';
import 'package:mobile/features/profile/presentation/edit_profile_screen.dart';
import 'package:mobile/features/profile/presentation/settings_screen.dart';
import 'package:mobile/features/profile/presentation/profile_screen.dart';
import 'package:mobile/features/tickets/presentation/tickets_screen.dart';
import 'package:mobile/features/tickets/presentation/ticket_details_screen.dart';
import 'package:mobile/features/admin/presentation/super_admin_dashboard.dart';
import 'package:mobile/features/admin/presentation/list_org_admins_screen.dart';
import 'package:mobile/features/admin/presentation/create_org_admin_screen.dart';
import 'package:mobile/features/admin/presentation/admin_org_dashboard.dart';
import 'package:mobile/features/admin/presentation/list_org_users_screen.dart';
import 'package:mobile/features/admin/presentation/create_regular_user_screen.dart';
import 'package:mobile/features/admin/presentation/manage_organizations_screen.dart';
import 'package:mobile/features/admin/presentation/manage_events_screen.dart';
import 'package:mobile/features/scanner/presentation/scanner_dashboard_screen.dart';
import 'package:mobile/features/scanner/presentation/qr_scanner_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final refreshNotifier = GoRouterRefreshNotifier();

  // Trigger redirects when auth state changes.
  ref.listen<AuthState>(authControllerProvider, (_, __) {
    refreshNotifier.notifyListeners();
  });

  return GoRouter(
    initialLocation: '/',
    debugLogDiagnostics: false,
    refreshListenable: refreshNotifier,
    redirect: (context, state) {
      final authState = ref.read(authControllerProvider);

      final isLoggedIn = authState.isAuthenticated && !authState.isExpired;
      final path = state.matchedLocation;

      final isPublic = path == '/' ||
          path == '/login' ||
          path == '/register' ||
          path == '/verify' ||
          path == '/forgot-password' ||
          path == '/reset-password';

      final isAuthRoute = path == '/login' ||
          path == '/register' ||
          path == '/verify' ||
          path == '/forgot-password' ||
          path == '/reset-password';

      if (!isLoggedIn && !isPublic) return '/login';

      if (isLoggedIn) {
        if (authState.userRole == 'SUPER_ADMIN' && !path.startsWith('/super-admin')) {
          return '/super-admin';
        }
        if (authState.userRole == 'ADMIN_ORG' && !path.startsWith('/admin-org')) {
          return '/admin-org';
        }
        if (authState.userRole == 'SCANNER' && !path.startsWith('/scanner')) {
          return '/scanner';
        }
        if (authState.userRole == 'GUEST' && isAuthRoute) {
          return '/app/home';
        }

        // Protect admin routes from non-admins
        if (path.startsWith('/super-admin') && authState.userRole != 'SUPER_ADMIN') {
          return '/unauthorized';
        }
        if (path.startsWith('/admin-org') && authState.userRole != 'ADMIN_ORG') {
          return '/unauthorized';
        }
        if (path.startsWith('/scanner') && authState.userRole != 'SCANNER') {
          return '/unauthorized';
        }
      }
      return null;
    },
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/verify',
        builder: (context, state) => const VerifyEmailScreen(),
      ),
      GoRoute(
        path: '/forgot-password',
        builder: (context, state) => const ForgotPasswordScreen(),
      ),
      GoRoute(
        path: '/reset-password',
        builder: (context, state) => const ResetPasswordScreen(),
      ),
      GoRoute(
        path: '/unauthorized',
        builder: (context, state) => const UnauthorizedScreen(),
      ),
      GoRoute(
        path: '/super-admin',
        builder: (context, state) => const SuperAdminDashboard(),
      ),
      GoRoute(
        path: '/super-admin/org-admins',
        builder: (context, state) => const ListOrgAdminsScreen(),
      ),
      GoRoute(
        path: '/super-admin/create-org-admin',
        builder: (context, state) => const CreateOrgAdminScreen(),
      ),
      GoRoute(
        path: '/admin-org',
        builder: (context, state) => const AdminOrgDashboard(),
      ),
      GoRoute(
        path: '/admin-org/users',
        builder: (context, state) => const ListOrgUsersScreen(),
      ),
      GoRoute(
        path: '/admin-org/create-user',
        builder: (context, state) => const CreateRegularUserScreen(),
      ),
      GoRoute(
        path: '/admin-org/events',
        builder: (context, state) => const ManageEventsScreen(),
      ),
      GoRoute(
        path: '/super-admin/organizations',
        builder: (context, state) => const ManageOrganizationsScreen(),
      ),
      GoRoute(
        path: '/scanner',
        builder: (context, state) => const ScannerDashboardScreen(),
      ),
      GoRoute(
        path: '/scanner/scan',
        builder: (context, state) => const QRScannerScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: '/app/home',
            builder: (context, state) => const HomeScreen(),
          ),
          GoRoute(
            path: '/app/orders',
            builder: (context, state) => const OrdersScreen(),
          ),
          GoRoute(
            path: '/app/orders/:orderId',
            builder: (context, state) {
              final orderId = state.pathParameters['orderId']!;
              return OrderDetailsScreen(orderId: orderId);
            },
          ),
          GoRoute(
            path: '/app/tickets',
            builder: (context, state) => const TicketsScreen(),
          ),
          GoRoute(
            path: '/app/tickets/:ticketId',
            builder: (context, state) {
              final ticketId = state.pathParameters['ticketId']!;
              return TicketDetailsScreen(ticketId: ticketId);
            },
          ),
          GoRoute(
            path: '/app/notifications',
            builder: (context, state) => const NotificationsScreen(),
          ),
          GoRoute(
            path: '/app/profile',
            builder: (context, state) => const ProfileScreen(),
          ),
          GoRoute(
            path: '/app/profile/edit',
            builder: (context, state) => const EditProfileScreen(),
          ),
          GoRoute(
            path: '/app/profile/settings',
            builder: (context, state) => const SettingsScreen(),
          ),
          GoRoute(
            path: '/app/payments',
            builder: (context, state) => const PaymentsScreen(),
          ),
          GoRoute(
            path: '/app/payments/add',
            builder: (context, state) => const AddPaymentScreen(),
          ),
          GoRoute(
            path: '/app/payments/process/:orderId',
            builder: (context, state) {
              final orderId = state.pathParameters['orderId']!;
              return PaymentProcessScreen(orderId: orderId);
            },
          ),
          GoRoute(
            path: '/app/payments/success/:orderId',
            builder: (context, state) {
              final orderId = state.pathParameters['orderId']!;
              return PaymentSuccessScreen(orderId: orderId);
            },
          ),
          GoRoute(
            path: '/app/payments/failure/:orderId',
            builder: (context, state) {
              final orderId = state.pathParameters['orderId']!;
              final reason = state.extra as String?;
              return PaymentFailureScreen(orderId: orderId, reason: reason);
            },
          ),
        ],
      ),
    ],
  );
});

class GoRouterRefreshNotifier extends ChangeNotifier {}

