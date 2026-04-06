import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/notifications/data/repositories/notifications_repository_impl.dart';
import 'package:mobile/features/notifications/domain/models/notification_models.dart';

final notificationsProvider = FutureProvider.autoDispose<List<NotificationDTO>>((ref) async {
  final auth = ref.watch(authControllerProvider);
  if (auth.userId == null || auth.accessToken == null) return const [];

  final repo = ref.read(notificationsRepositoryProvider);
  final page = await repo.fetchNotificationsByUser(
    userId: auth.userId!,
    page: 0,
    size: 50,
    bearerToken: auth.accessToken!,
  );
  return page.content;
});

class NotificationsScreen extends ConsumerWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notificationsAsync = ref.watch(notificationsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Notifications')),
      body: notificationsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, st) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Failed to load notifications', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 10),
                Text(err.toString()),
                const SizedBox(height: 16),
                FilledButton.tonal(
                  onPressed: () => ref.invalidate(notificationsProvider),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
        data: (notifications) {
          if (notifications.isEmpty) {
            return const _EmptyNotificationsState();
          }

          final sorted = [...notifications]
            ..sort((a, b) => b.createdAt.compareTo(a.createdAt));

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: sorted.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final n = sorted[index];
              return Dismissible(
                key: ValueKey(n.id),
                background: Container(
                  decoration: BoxDecoration(
                    color: Colors.red.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  alignment: Alignment.centerLeft,
                  padding: const EdgeInsets.symmetric(horizontal: 18),
                  child: const Icon(Icons.delete_outline, color: Colors.red),
                ),
                direction: DismissDirection.endToStart,
                confirmDismiss: (_) async {
                  return true;
                },
                onDismissed: (_) async {
                  final auth = ref.read(authControllerProvider);
                  final token = auth.accessToken;
                  if (token == null) return;
                  try {
                    await ref.read(notificationsRepositoryProvider).deleteNotification(
                          notificationId: n.id,
                          bearerToken: token,
                        );
                    if (!context.mounted) return;
                    ref.invalidate(notificationsProvider);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Notification deleted')),
                    );
                  } catch (e) {
                    if (!context.mounted) return;
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text(e.toString()), backgroundColor: Colors.red),
                    );
                    ref.invalidate(notificationsProvider);
                  }
                },
                child: Card(
                  elevation: 0,
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(14),
                    leading: Icon(
                      n.read ? Icons.notifications_none_outlined : Icons.notifications_active,
                      color: n.read
                          ? Theme.of(context).colorScheme.primary.withOpacity(0.6)
                          : Theme.of(context).colorScheme.primary,
                    ),
                    title: Text(
                      n.subject ?? 'Notification',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontWeight: n.read ? FontWeight.w600 : FontWeight.w800,
                      ),
                    ),
                    subtitle: Text(
                      n.content ?? '',
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    trailing: Text(
                      _formatTime(n.createdAt),
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                    onTap: () async {
                      if (n.read) return;
                      final auth = ref.read(authControllerProvider);
                      if (auth.accessToken == null) return;
                      await ref.read(notificationsRepositoryProvider).markAsRead(
                            notificationId: n.id,
                            bearerToken: auth.accessToken!,
                          );
                      ref.invalidate(notificationsProvider);
                    },
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }

  String _formatTime(DateTime dt) {
    final h = dt.hour.toString().padLeft(2, '0');
    final m = dt.minute.toString().padLeft(2, '0');
    return '$h:$m';
  }
}

class _EmptyNotificationsState extends StatelessWidget {
  const _EmptyNotificationsState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.notifications_off_outlined, size: 64, color: Theme.of(context).colorScheme.primary.withOpacity(0.7)),
            const SizedBox(height: 10),
            Text('No notifications', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(
              'You will receive updates about tickets, payments and events here.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

