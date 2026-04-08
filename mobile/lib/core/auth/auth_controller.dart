import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:dio/dio.dart';
import 'package:mobile/core/auth/token_storage.dart';
import 'package:mobile/core/config/app_env.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/auth/auth_state.dart';

final authControllerProvider =
    StateNotifierProvider<AuthController, AuthState>(
  (ref) => AuthController(
    tokenStorage: const TokenStorage(),
    apiClient: ApiClient.create(AppEnv.apiBaseUrl),
  )..initialize(),
);

class AuthController extends StateNotifier<AuthState> {
  AuthController({
    required TokenStorage tokenStorage,
    required ApiClient apiClient,
  })  : _tokenStorage = tokenStorage,
        _apiClient = apiClient,
        super(const AuthState.unauthenticated());

  final TokenStorage _tokenStorage;
  final ApiClient _apiClient;

  final Dio _keycloakDio = Dio();

  Future<void> initialize() async {
    final accessToken = await _tokenStorage.readAccessToken();
    final refreshToken = await _tokenStorage.readRefreshToken();
    final expiresAtMs = await _tokenStorage.readExpiresAtMs();
    final userId = await _tokenStorage.readUserId();
    final userEmail = await _tokenStorage.readUserEmail();
    final userName = await _tokenStorage.readUserName();

    if (accessToken == null || refreshToken == null || expiresAtMs == null) {
      state = const AuthState.unauthenticated();
      return;
    }

    if (expiresAtMs - 60 * 1000 > DateTime.now().millisecondsSinceEpoch) {
      state = AuthState(
        accessToken: accessToken,
        refreshToken: refreshToken,
        expiresAtMs: expiresAtMs,
        userId: userId,
        userEmail: userEmail,
        userName: userName,
      );
      return;
    }

    final refreshed = await _refreshTokens(refreshToken);
    if (refreshed == null) {
      await _tokenStorage.clear();
      state = const AuthState.unauthenticated();
      return;
    }

    state = AuthState(
      accessToken: refreshed.accessToken,
      refreshToken: refreshed.refreshToken,
      expiresAtMs: refreshed.expiresAtMs,
      userId: userId,
      userEmail: userEmail,
      userName: userName,
    );
  }

  Future<void> signIn({
    required String email,
    required String password,
  }) async {
    // POST /api/auth/login -> ApiResponse<LoginResponse>
    final apiResponse = await _apiClient.postApiResponseDynamic(
      '/api/auth/login',
      body: {'email': email, 'password': password},
    );

    if (!apiResponse.success) {
      throw Exception(apiResponse.error?.message ?? 'Login failed');
    }

    final data = apiResponse.data as Map<String, dynamic>;
    final accessToken = data['accessToken'] as String?;
    final refreshToken = data['refreshToken'] as String?;
    final tokenType = data['tokenType'] as String?;
    final expiresIn = (data['expiresIn'] as num?)?.toInt();

    if (accessToken == null ||
        refreshToken == null ||
        expiresIn == null ||
        tokenType == null) {
      throw Exception('Invalid login response');
    }

    final expiresAtMs =
        DateTime.now().millisecondsSinceEpoch + expiresIn * 1000;

    // Decode keycloak sub to map to user-service UUID.
    final keycloakSub = JwtDecoder.decode(accessToken)['sub'] as String?;
    if (keycloakSub == null || keycloakSub.isEmpty) {
      throw Exception('Missing JWT subject (sub)');
    }

    final userResp = await _apiClient.getApiResponse<Map<String, dynamic>>(
      '/api/users/keycloak/$keycloakSub',
      bearerToken: accessToken,
      dataParser: (json) => json as Map<String, dynamic>,
    );

    if (!userResp.success) {
      throw Exception(userResp.error?.message ?? 'Failed to fetch user');
    }

    final userDto = userResp.data!;
    final userId = userDto['id']?.toString();
    final userEmail = userDto['email']?.toString() ?? email;
    final firstName = userDto['firstName']?.toString() ?? '';
    final lastName = userDto['lastName']?.toString() ?? '';
    final userName = (firstName.isNotEmpty || lastName.isNotEmpty)
        ? [firstName, lastName].where((e) => e.isNotEmpty).join(' ')
        : email;

    if (userId == null) {
      throw Exception('User id missing in user-service response');
    }

    await _tokenStorage.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken,
      expiresAtMs: expiresAtMs.toInt(),
    );
    await _tokenStorage.saveUser(
      userId: userId,
      email: userEmail,
      name: userName,
    );

    state = AuthState(
      accessToken: accessToken,
      refreshToken: refreshToken,
      expiresAtMs: expiresAtMs.toInt(),
      userId: userId,
      userEmail: userEmail,
      userName: userName,
    );
  }

  Future<void> signUp({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
  }) async {
    final apiResponse = await _apiClient.postApiResponseDynamic(
      '/api/auth/register',
      body: {
        'username': email,
        'email': email,
        'password': password,
        'firstName': firstName,
        'lastName': lastName,
        'phone': null,
      },
    );

    if (!apiResponse.success) {
      throw Exception(apiResponse.error?.message ?? 'Registration failed');
    }
  }

  Future<void> verifyEmail({required String token}) async {
    final apiResponse = await _apiClient.postApiResponseDynamic(
      '/api/auth/email/verify',
      queryParameters: {'token': token},
    );

    if (!apiResponse.success) {
      throw Exception(apiResponse.error?.message ?? 'Email verification failed');
    }
  }

  Future<void> requestPasswordReset({required String email}) async {
    final apiResponse = await _apiClient.postApiResponseDynamic(
      '/api/auth/password/reset-request',
      queryParameters: {'email': email},
    );
    if (!apiResponse.success) {
      throw Exception(
        apiResponse.error?.message ?? 'Password reset request failed',
      );
    }
  }

  Future<void> resetPassword({
    required String token,
    required String newPassword,
  }) async {
    final apiResponse = await _apiClient.postApiResponseDynamic(
      '/api/auth/password/reset',
      body: {'token': token, 'newPassword': newPassword},
    );
    if (!apiResponse.success) {
      throw Exception(apiResponse.error?.message ?? 'Reset failed');
    }
  }

  Future<void> signOut() async {
    await _tokenStorage.clear();
    state = const AuthState.unauthenticated();
  }

  Future<_TokenRefreshResult?> _refreshTokens(String refreshToken) async {
    try {
      final url =
          '${AppEnv.keycloakBaseUrl}/realms/${AppEnv.keycloakRealm}/protocol/openid-connect/token';

      final form = FormData.fromMap({
        'grant_type': 'refresh_token',
        'client_id': AppEnv.keycloakClientId,
        'client_secret': AppEnv.keycloakClientSecret,
        'refresh_token': refreshToken,
      });

      final resp = await _keycloakDio.post<Map<String, dynamic>>(
        url,
        data: form,
        options: Options(
          headers: const {'Content-Type': 'application/x-www-form-urlencoded'},
        ),
      );

      final data = resp.data;
      if (data == null) return null;

      final newAccessToken = data['access_token']?.toString();
      final newRefreshToken = data['refresh_token']?.toString();
      final expiresIn = (data['expires_in'] as num?)?.toInt();

      if (newAccessToken == null ||
          newRefreshToken == null ||
          expiresIn == null) {
        return null;
      }

      final expiresAtMs = DateTime.now().millisecondsSinceEpoch + expiresIn * 1000;

      await _tokenStorage.saveTokens(
        accessToken: newAccessToken,
        refreshToken: newRefreshToken,
        expiresAtMs: expiresAtMs,
      );

      return _TokenRefreshResult(
        accessToken: newAccessToken,
        refreshToken: newRefreshToken,
        expiresAtMs: expiresAtMs,
      );
    } catch (_) {
      return null;
    }
  }
}

class _TokenRefreshResult {
  final String accessToken;
  final String refreshToken;
  final int expiresAtMs;

  const _TokenRefreshResult({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresAtMs,
  });
}

