import 'dart:io' show Platform;

class AppEnv {
  static String get apiBaseUrl {
    // Default for Android emulator
    if (Platform.isAndroid) {
      return const String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');
    }
    // iOS simulator / device (adjust if needed)
    return const String.fromEnvironment('API_BASE_URL', defaultValue: 'http://localhost:8080');
  }

  static String get keycloakBaseUrl =>
      const String.fromEnvironment('KEYCLOAK_BASE_URL', defaultValue: 'http://localhost:8180');

  static String get keycloakRealm =>
      const String.fromEnvironment('KEYCLOAK_REALM', defaultValue: 'tisqra');

  static String get keycloakClientId =>
      const String.fromEnvironment('KEYCLOAK_CLIENT_ID', defaultValue: 'event-ticketing-client');

  static String get keycloakClientSecret =>
      const String.fromEnvironment('KEYCLOAK_CLIENT_SECRET', defaultValue: '');
}

