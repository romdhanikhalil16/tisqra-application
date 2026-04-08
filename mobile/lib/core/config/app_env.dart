import 'dart:io' show Platform;

import 'package:flutter_dotenv/flutter_dotenv.dart';

class AppEnv {
  static String get apiBaseUrl {
    final envUrl = dotenv.env['API_BASE_URL']?.trim();
    if (envUrl != null && envUrl.isNotEmpty) return envUrl;

    // Default for Android emulator
    if (Platform.isAndroid) {
      return const String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');
    }
    // iOS simulator / device (adjust if needed)
    return const String.fromEnvironment('API_BASE_URL', defaultValue: 'http://localhost:8080');
  }

  static String get keycloakBaseUrl {
    final v = dotenv.env['KEYCLOAK_BASE_URL']?.trim();
    if (v != null && v.isNotEmpty) return v;
    return const String.fromEnvironment('KEYCLOAK_BASE_URL', defaultValue: 'http://localhost:8180');
  }

  static String get keycloakRealm {
    final v = dotenv.env['KEYCLOAK_REALM']?.trim();
    if (v != null && v.isNotEmpty) return v;
    return const String.fromEnvironment('KEYCLOAK_REALM', defaultValue: 'event-ticketing');
  }

  static String get keycloakClientId {
    final v = dotenv.env['KEYCLOAK_CLIENT_ID']?.trim();
    if (v != null && v.isNotEmpty) return v;
    return const String.fromEnvironment('KEYCLOAK_CLIENT_ID', defaultValue: 'event-ticketing-client');
  }

  static String get keycloakClientSecret {
    final v = dotenv.env['KEYCLOAK_CLIENT_SECRET'];
    if (v != null) return v;
    return const String.fromEnvironment('KEYCLOAK_CLIENT_SECRET', defaultValue: '');
  }
}

