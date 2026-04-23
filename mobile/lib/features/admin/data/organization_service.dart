import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/api_response.dart';
import 'package:mobile/core/providers/api_providers.dart';

final organizationServiceProvider = Provider<OrganizationService>((ref) {
  final apiClient = ref.read(apiClientProvider);
  return OrganizationService(apiClient);
});

class OrganizationService {
  final ApiClient _apiClient;

  OrganizationService(this._apiClient);

  Future<ApiResponse<dynamic>> getOrganizations(String token, {int page = 0, int size = 20}) {
    return _apiClient.getApiResponse<dynamic>(
      '/api/organizations',
      bearerToken: token,
      queryParameters: {'page': page, 'size': size},
      dataParser: (json) => json,
    );
  }

  Future<ApiResponse<dynamic>> getOrganizationById(String token, String id) {
    return _apiClient.getApiResponse<dynamic>(
      '/api/organizations/$id',
      bearerToken: token,
      dataParser: (json) => json,
    );
  }

  Future<ApiResponse<dynamic>> createOrganization(
    String token, {
    required String name,
    required String email,
    required String phone,
    required String address,
    required String city,
    required String country,
  }) {
    return _apiClient.postApiResponseDynamic(
      '/api/organizations',
      bearerToken: token,
      body: {
        'name': name,
        'email': email,
        'phone': phone,
        'address': address,
        'city': city,
        'country': country,
      },
    );
  }

  Future<ApiResponse<dynamic>> updateOrganization(
    String token,
    String id, {
    required String name,
    required String email,
    required String phone,
    required String address,
    required String city,
    required String country,
  }) {
    return _apiClient.putApiResponseDynamic(
      '/api/organizations/$id',
      bearerToken: token,
      body: {
        'name': name,
        'email': email,
        'phone': phone,
        'address': address,
        'city': city,
        'country': country,
      },
    );
  }
}
