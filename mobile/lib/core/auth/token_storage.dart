import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStorage {
  const TokenStorage();

  static const _kAccessToken = 'access_token';
  static const _kRefreshToken = 'refresh_token';
  static const _kUserId = 'user_id';
  static const _kUserEmail = 'user_email';
  static const _kUserName = 'user_name';
  static const _kUserRole = 'user_role';
  static const _kExpiresAt = 'expires_at_ms';

  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
    required int expiresAtMs,
  }) async {
    await _storage.write(key: _kAccessToken, value: accessToken);
    await _storage.write(key: _kRefreshToken, value: refreshToken);
    await _storage.write(key: _kExpiresAt, value: expiresAtMs.toString());
  }

  Future<void> saveUser({
    required String userId,
    required String email,
    required String name,
    required String role,
  }) async {
    await _storage.write(key: _kUserId, value: userId);
    await _storage.write(key: _kUserEmail, value: email);
    await _storage.write(key: _kUserName, value: name);
    await _storage.write(key: _kUserRole, value: role);
  }

  Future<String?> readAccessToken() => _storage.read(key: _kAccessToken);
  Future<String?> readRefreshToken() => _storage.read(key: _kRefreshToken);
  Future<int?> readExpiresAtMs() async {
    final v = await _storage.read(key: _kExpiresAt);
    if (v == null) return null;
    return int.tryParse(v);
  }

  Future<String?> readUserId() => _storage.read(key: _kUserId);
  Future<String?> readUserEmail() => _storage.read(key: _kUserEmail);
  Future<String?> readUserName() => _storage.read(key: _kUserName);
  Future<String?> readUserRole() => _storage.read(key: _kUserRole);

  Future<void> clear() async {
    await _storage.delete(key: _kAccessToken);
    await _storage.delete(key: _kRefreshToken);
    await _storage.delete(key: _kUserId);
    await _storage.delete(key: _kUserEmail);
    await _storage.delete(key: _kUserName);
    await _storage.delete(key: _kUserRole);
    await _storage.delete(key: _kExpiresAt);
  }
}

