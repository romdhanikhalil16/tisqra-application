import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/notifications/domain/models/notification_models.dart';

abstract class NotificationsRepository {
  Future<PageResult<NotificationDTO>> fetchNotificationsByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  });

  Future<void> markAsRead({
    required String notificationId,
    required String bearerToken,
  });

  Future<void> deleteNotification({
    required String notificationId,
    required String bearerToken,
  });
}

