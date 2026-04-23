import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/api_response.dart';
import 'package:mobile/core/providers/api_providers.dart';

final userServiceProvider = Provider<UserService>((ref) {
  final apiClient = ref.read(apiClientProvider);
  return UserService(apiClient);
});

class UserService {
  final ApiClient _apiClient;

  UserService(this._apiClient);

  Future<ApiResponse<dynamic>> getUsers(String token, {int page = 0, int size = 20}) {
    return _apiClient.getApiResponse<dynamic>(
      '/api/users',
      bearerToken: token,
      queryParameters: {'page': page, 'size': size},
      dataParser: (json) => json, // Let Riverpod handle casting later
    );
  }

  Future<ApiResponse<dynamic>> provisionUser(
    String token, {
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    required String role,
    String? phone,
  }) {
    return _apiClient.postApiResponseDynamic(
      '/api/users/provision',
      bearerToken: token,
      body: {
        'email': email,
        'password': password,
        'firstName': firstName,
        'lastName': lastName,
        'role': role,
        'phone': phone,
      },
    );
  }

  Future<ApiResponse<dynamic>> createUser(
    String token, {
    required String email,
    required String firstName,
    required String lastName,
    required String role,
    String? keycloakId,
    String? phone,
  }) {
    return _apiClient.postApiResponseDynamic(
      '/api/users',
      bearerToken: token,
      body: {
        'email': email,
        'keycloakId': keycloakId ?? '', // keycloakId required in body, could be empty or fetched
        'firstName': firstName,
        'lastName': lastName,
        'role': role,
        'phone': phone,
      },
    );
  }

  Future<ApiResponse<dynamic>> deactivateUser(String token, String userId) {
    return _apiClient.postApiResponseDynamic(
      '/api/users/$userId/deactivate',
      bearerToken: token,
    );
  }

  Future<ApiResponse<dynamic>> activateUser(String token, String userId) {
    return _apiClient.postApiResponseDynamic(
      '/api/users/$userId/activate',
      bearerToken: token,
    );
  }

  Future<ApiResponse<dynamic>> deleteUser(String token, String userId) {
    return _apiClient.deleteApiResponseDynamic(
      '/api/users/$userId/permanent',
      bearerToken: token,
    );
  }
}
