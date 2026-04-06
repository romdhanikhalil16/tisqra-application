import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/orders/data/repositories/orders_repository_impl.dart';
import 'package:mobile/features/orders/domain/models/order_models.dart';

final ordersPageProvider = FutureProvider.autoDispose<List<OrderDTO>>((ref) async {
  final auth = ref.watch(authControllerProvider);
  final repo = ref.watch(ordersRepositoryProvider);
  final token = auth.accessToken;
  final userId = auth.userId;

  if (token == null || userId == null) {
    return const [];
  }

  final page = await repo.fetchOrdersByUser(
    userId: userId,
    page: 0,
    size: 50,
    bearerToken: token,
  );
  return page.content;
});

class OrdersScreen extends ConsumerStatefulWidget {
  const OrdersScreen({super.key});

  @override
  ConsumerState<OrdersScreen> createState() => _OrdersScreenState();
}

class _OrdersScreenState extends ConsumerState<OrdersScreen>
    with SingleTickerProviderStateMixin {
  final _searchCtrl = TextEditingController();
  TabController? _tabController;

  @override
  void dispose() {
    _searchCtrl.dispose();
    _tabController?.dispose();
    super.dispose();
  }

  bool _isCancelled(OrderStatus s) =>
      s == OrderStatus.CANCELLED || s == OrderStatus.REFUNDED || s == OrderStatus.EXPIRED;

  bool _isActive(OrderStatus s) =>
      s == OrderStatus.PENDING || s == OrderStatus.CONFIRMED || s == OrderStatus.PROCESSING;

  @override
  Widget build(BuildContext context) {
    final ordersAsync = ref.watch(ordersPageProvider);

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Orders'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Active'),
              Tab(text: 'Completed'),
              Tab(text: 'Cancelled'),
            ],
          ),
        ),
        body: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: TextField(
                controller: _searchCtrl,
                decoration: InputDecoration(
                  hintText: 'Search by order number…',
                  prefixIcon: const Icon(Icons.search_outlined),
                  border: const OutlineInputBorder(),
                  suffixIcon: IconButton(
                    icon: const Icon(Icons.clear),
                    onPressed: () => _searchCtrl.clear(),
                  ),
                ),
                onChanged: (_) => setState(() {}),
              ),
            ),
            Expanded(
              child: ordersAsync.when(
                data: (orders) {
                  final tabIndex = DefaultTabController.of(context).index;
                  final q = _searchCtrl.text.trim().toLowerCase();

                  List<OrderDTO> filtered = orders.where((o) {
                    if (tabIndex == 1) return o.status == OrderStatus.COMPLETED;
                    if (tabIndex == 2) return _isCancelled(o.status);
                    return _isActive(o.status);
                  }).toList();

                  if (q.isNotEmpty) {
                    filtered = filtered.where((o) => o.orderNumber.toLowerCase().contains(q)).toList();
                  }

                  filtered.sort((a, b) => b.createdAt.compareTo(a.createdAt));

                  if (filtered.isEmpty) {
                    return Center(
                      child: Padding(
                        padding: const EdgeInsets.all(24),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.receipt_long_outlined, size: 54, color: Theme.of(context).colorScheme.primary.withOpacity(0.6)),
                            const SizedBox(height: 12),
                            Text(
                              'No orders found',
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                            const SizedBox(height: 6),
                            Text(
                              'Try adjusting the filters or search query.',
                              textAlign: TextAlign.center,
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          ],
                        ),
                      ),
                    );
                  }

                  return ListView.separated(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
                    itemCount: filtered.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final order = filtered[index];
                      final dateLabel = DateFormat.yMMMd().format(order.createdAt);
                      return Card(
                        elevation: 0,
                        child: ListTile(
                          contentPadding: const EdgeInsets.all(14),
                          title: Text(
                            order.orderNumber,
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          subtitle: Text(
                            '$dateLabel • ${order.currency} ${order.totalAmount.toStringAsFixed(2)}',
                          ),
                          trailing: _StatusBadge(status: order.status),
                          onTap: () => context.go('/app/orders/${order.id}'),
                        ),
                      );
                    },
                  );
                },
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (err, st) => Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text('Failed to load orders', style: TextStyle(color: Theme.of(context).colorScheme.error)),
                        const SizedBox(height: 8),
                        Text(err.toString()),
                        const SizedBox(height: 14),
                        FilledButton.tonal(
                          onPressed: () => ref.invalidate(ordersPageProvider),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final OrderStatus status;
  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    final isCompleted = status == OrderStatus.COMPLETED;
    final isCancelled = status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED || status == OrderStatus.EXPIRED;
    final color = isCompleted
        ? Colors.green
        : isCancelled
            ? Colors.grey
            : Theme.of(context).colorScheme.primary;

    return Chip(
      label: Text(status.name),
      backgroundColor: color.withOpacity(0.15),
      labelStyle: TextStyle(color: color),
    );
  }
}

