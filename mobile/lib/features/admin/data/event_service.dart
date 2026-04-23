import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/api_response.dart';
import 'package:mobile/core/providers/api_providers.dart';

final eventServiceProvider = Provider<EventService>((ref) {
  final apiClient = ref.read(apiClientProvider);
  return EventService(apiClient);
});

class EventService {
  final ApiClient _apiClient;

  EventService(this._apiClient);

  Future<ApiResponse<dynamic>> createEvent(
    String token, {
    required String organizationId,
    required String name,
    required String description,
    required String slug,
    required String category,
    required String startDate,
    required String endDate,
    required int capacity,
    required List<Map<String, dynamic>> ticketCategories,
  }) {
    return _apiClient.postApiResponseDynamic(
      '/api/events',
      bearerToken: token,
      body: {
        'organizationId': organizationId,
        'name': name,
        'description': description,
        'slug': slug,
        'category': category,
        'startDate': startDate,
        'endDate': endDate,
        'capacity': capacity,
        'location': {
          'name': 'Main Venue',
          'address': '123 Event St',
          'city': 'Tunis',
          'state': 'Tunis',
          'country': 'TN',
          'zipCode': '1000',
          'latitude': 36.8065,
          'longitude': 10.1815,
        },
        'ticketCategories': ticketCategories,
        'scheduleItems': [],
      },
    );
  }

  Future<ApiResponse<dynamic>> getEvents(String token, {int page = 0, int size = 20}) {
    return _apiClient.getApiResponse<dynamic>(
      '/api/events/search', // We will use search for general listing if /api/events isn't available
      bearerToken: token,
      queryParameters: {'page': page, 'size': size},
      dataParser: (json) => json,
    );
  }

  Future<ApiResponse<dynamic>> publishEvent(String token, String id) {
    return _apiClient.postApiResponseDynamic(
      '/api/events/$id/publish',
      bearerToken: token,
    );
  }

  Future<ApiResponse<dynamic>> cancelEvent(String token, String id) {
    return _apiClient.postApiResponseDynamic(
      '/api/events/$id/cancel',
      bearerToken: token,
    );
  }
}
