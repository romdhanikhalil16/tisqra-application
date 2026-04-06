import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/notifications/domain/models/notification_models.dart';
import 'package:mobile/features/notifications/domain/repositories/notifications_repository.dart';
import 'package:mobile/core/providers/api_providers.dart';

final notificationsRepositoryProvider = Provider<NotificationsRepository>(
  (ref) => NotificationsRepositoryImpl(
    apiClient: ref.watch(apiClientProvider),
  ),
);

class NotificationsRepositoryImpl implements NotificationsRepository {
  NotificationsRepositoryImpl({required this.apiClient});
  final ApiClient apiClient;

  @override
  Future<PageResult<NotificationDTO>> fetchNotificationsByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/notifications/user/$userId',
      queryParameters: {'page': page, 'size': size},
      bearerToken: bearerToken,
      dataParser: (json) {
        return PageResult.fromJson(
          json,
          itemParser: (item) =>
              NotificationDTO.fromJson(item as Map<String, dynamic>),
        );
      },
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to load notifications');
    }
    return apiResp.data!;
  }

  @override
  Future<void> markAsRead({
    required String notificationId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.postApiResponseDynamic(
      '/api/notifications/$notificationId/read',
      bearerToken: bearerToken,
    );
    if (!apiResp.success) {
      throw Exception(apiResp.error?.message ?? 'Failed to mark as read');
    }
  }

  @override
  Future<void> deleteNotification({
    required String notificationId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.deleteApiResponseDynamic(
      '/api/notifications/$notificationId',
      bearerToken: bearerToken,
    );
    if (!apiResp.success) {
      throw Exception(apiResp.error?.message ?? 'Failed to delete notification');
    }
  }
}

