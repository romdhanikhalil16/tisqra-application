import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/orders/data/repositories/orders_repository_impl.dart';
import 'package:mobile/features/orders/domain/models/order_models.dart';
import 'package:mobile/features/tickets/data/repositories/tickets_repository_impl.dart';
import 'package:mobile/features/tickets/domain/models/ticket_models.dart';
import 'package:mobile/features/notifications/data/repositories/notifications_repository_impl.dart';

final homeSnapshotProvider = FutureProvider.autoDispose<_HomeSnapshot>((ref) async {
  final auth = ref.watch(authControllerProvider);
  final userId = auth.userId;
  final token = auth.accessToken;

  if (userId == null || token == null) {
    throw Exception('Not authenticated');
  }

  final ordersRepo = ref.read(ordersRepositoryProvider);
  final ticketsRepo = ref.read(ticketsRepositoryProvider);
  final notificationsRepo = ref.read(notificationsRepositoryProvider);

  final orders = await ordersRepo.fetchOrdersByUser(
    userId: userId,
    page: 0,
    size: 50,
    bearerToken: token,
  );
  final tickets = await ticketsRepo.fetchTicketsByUser(
    userId: userId,
    page: 0,
    size: 50,
    bearerToken: token,
  );
  final notifications = await notificationsRepo.fetchNotificationsByUser(
    userId: userId,
    page: 0,
    size: 20,
    bearerToken: token,
  );

  final activeOrdersCount = orders.content.where(
    (o) => o.status == OrderStatus.PENDING || o.status == OrderStatus.CONFIRMED || o.status == OrderStatus.PROCESSING,
  ).length;

  final ticketsCount = tickets.content.where(
    (t) => t.status != TicketStatus.CANCELLED && t.status != TicketStatus.REFUNDED,
  ).length;

  final unreadNotificationsCount = notifications.content.where((n) => !n.read).length;

  final recent = <_RecentItem>[];
  for (final o in orders.content) {
    recent.add(_RecentItem(
      when: o.createdAt,
      title: 'Order ${o.orderNumber}',
      subtitle: o.status.name,
      icon: Icons.receipt_long_outlined,
    ));
  }
  for (final t in tickets.content) {
    recent.add(_RecentItem(
      when: t.validatedAt ?? DateTime.now(),
      title: 'Ticket ${t.ticketNumber}',
      subtitle: t.status.name,
      icon: Icons.confirmation_num_outlined,
    ));
  }
  for (final n in notifications.content) {
    recent.add(_RecentItem(
      when: n.createdAt,
      title: n.subject ?? 'Notification',
      subtitle: n.read ? 'Read' : 'New',
      icon: Icons.notifications_none_outlined,
    ));
  }
  recent.sort((a, b) => b.when.compareTo(a.when));

  return _HomeSnapshot(
    activeOrdersCount: activeOrdersCount,
    ticketsCount: ticketsCount,
    unreadNotificationsCount: unreadNotificationsCount,
    recentItems: recent.take(6).toList(),
  );
});

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);
    final snapshot = ref.watch(homeSnapshotProvider);

    return snapshot.when(
      data: (data) {
        return SafeArea(
          child: RefreshIndicator(
            onRefresh: () async {
              ref.invalidate(homeSnapshotProvider);
              await Future<void>.delayed(const Duration(milliseconds: 250));
            },
            child: ListView(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 28),
              children: [
                _Greeting(name: auth.userName ?? 'there'),
                const SizedBox(height: 16),
                _SummaryCards(
                  activeOrders: data.activeOrdersCount,
                  ticketsCount: data.ticketsCount,
                  notificationsUnread: data.unreadNotificationsCount,
                ),
                const SizedBox(height: 16),
                _QuickActions(
                  onGoOrders: () => context.go('/app/orders'),
                  onGoTickets: () => context.go('/app/tickets'),
                  onGoNotifications: () => context.go('/app/notifications'),
                ),
                const SizedBox(height: 20),
                _SectionTitle(title: 'Recent activity'),
                const SizedBox(height: 10),
                _RecentActivityList(items: data.recentItems),
              ],
            ),
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, st) => Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Failed to load dashboard', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 10),
              Text(err.toString()),
              const SizedBox(height: 16),
              FilledButton.tonal(
                onPressed: () => ref.invalidate(homeSnapshotProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _HomeSnapshot {
  final int activeOrdersCount;
  final int ticketsCount;
  final int unreadNotificationsCount;
  final List<_RecentItem> recentItems;

  _HomeSnapshot({
    required this.activeOrdersCount,
    required this.ticketsCount,
    required this.unreadNotificationsCount,
    required this.recentItems,
  });
}

class _RecentItem {
  final DateTime when;
  final String title;
  final String subtitle;
  final IconData icon;

  const _RecentItem({
    required this.when,
    required this.title,
    required this.subtitle,
    required this.icon,
  });
}

class _Greeting extends StatelessWidget {
  final String name;
  const _Greeting({required this.name});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Hello, $name', style: Theme.of(context).textTheme.headlineSmall),
        const SizedBox(height: 6),
        Text(
          'Manage your orders, tickets and notifications.',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _SummaryCards extends StatelessWidget {
  final int activeOrders;
  final int ticketsCount;
  final int notificationsUnread;

  const _SummaryCards({
    required this.activeOrders,
    required this.ticketsCount,
    required this.notificationsUnread,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _StatCard(
            icon: Icons.receipt_long_outlined,
            label: 'Active orders',
            value: activeOrders.toString(),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _StatCard(
            icon: Icons.confirmation_num_outlined,
            label: 'Tickets',
            value: ticketsCount.toString(),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _StatCard(
            icon: Icons.notifications_none_outlined,
            label: 'Notifications',
            value: notificationsUnread.toString(),
            valueColor: notificationsUnread > 0 ? Colors.deepPurple : null,
          ),
        ),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color? valueColor;

  const _StatCard({
    required this.icon,
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      color: Theme.of(context).colorScheme.surfaceContainerHighest,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: Theme.of(context).colorScheme.primary),
            const SizedBox(height: 10),
            Text(
              value,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    color: valueColor ?? Theme.of(context).colorScheme.primary,
                    fontWeight: FontWeight.w800,
                  ),
            ),
            const SizedBox(height: 6),
            Text(label, style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  final VoidCallback onGoOrders;
  final VoidCallback onGoTickets;
  final VoidCallback onGoNotifications;

  const _QuickActions({
    required this.onGoOrders,
    required this.onGoTickets,
    required this.onGoNotifications,
  });

  @override
  Widget build(BuildContext context) {
    return Wrap(
      runSpacing: 12,
      spacing: 12,
      children: [
        _ActionChip(icon: Icons.receipt_long, label: 'Orders', onTap: onGoOrders),
        _ActionChip(
          icon: Icons.confirmation_num,
          label: 'Tickets',
          onTap: onGoTickets,
        ),
        _ActionChip(
          icon: Icons.notifications,
          label: 'Notifications',
          onTap: onGoNotifications,
        ),
      ],
    );
  }
}

class _ActionChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionChip({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return FilledButton.tonalIcon(
      icon: Icon(icon, size: 18),
      label: Text(label),
      onPressed: onTap,
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String title;
  const _SectionTitle({required this.title});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Text(title, style: Theme.of(context).textTheme.titleLarge),
        const Spacer(),
        Text(DateFormat.yMMMMd().format(DateTime.now()), style: Theme.of(context).textTheme.bodySmall),
      ],
    );
  }
}

class _RecentActivityList extends StatelessWidget {
  final List<_RecentItem> items;
  const _RecentActivityList({required this.items});

  @override
  Widget build(BuildContext context) {
    if (items.isEmpty) return const _EmptyActivityState();
    return ListView.separated(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: items.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final item = items[index];
        return Card(
          elevation: 0,
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: Theme.of(context).colorScheme.primary.withOpacity(0.12),
              child: Icon(item.icon, color: Theme.of(context).colorScheme.primary),
            ),
            title: Text(item.title),
            subtitle: Text('${item.subtitle} • ${DateFormat.Hm().format(item.when)}'),
          ),
        );
      },
    );
  }
}

class _EmptyActivityState extends StatelessWidget {
  const _EmptyActivityState();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.history, size: 52, color: Theme.of(context).colorScheme.primary.withOpacity(0.7)),
          const SizedBox(height: 10),
          Text('No recent activity', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 4),
          Text(
            'Your activity will appear here.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}

