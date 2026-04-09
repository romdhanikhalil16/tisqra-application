class AuthState {
  final String? accessToken;
  final String? refreshToken;
  final int? expiresAtMs;
  final String? userId;
  final String? userEmail;
  final String? userName;
  final String? userRole;

  const AuthState({
    this.accessToken,
    this.refreshToken,
    this.expiresAtMs,
    this.userId,
    this.userEmail,
    this.userName,
    this.userRole,
  });

  const AuthState.unauthenticated()
      : accessToken = null,
        refreshToken = null,
        expiresAtMs = null,
        userId = null,
        userEmail = null,
        userName = null,
        userRole = null;

  bool get isAuthenticated => accessToken != null && expiresAtMs != null;

  bool get isExpired {
    if (expiresAtMs == null) return true;
    final now = DateTime.now().millisecondsSinceEpoch;
    // Small skew to avoid edge failures
    return expiresAtMs! - 60 * 1000 < now;
  }
}

