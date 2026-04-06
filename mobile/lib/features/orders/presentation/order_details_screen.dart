import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/orders/data/repositories/orders_repository_impl.dart';
import 'package:mobile/features/orders/domain/models/order_models.dart';
import 'package:mobile/features/payments/data/repositories/payments_repository_impl.dart';
import 'package:mobile/features/payments/domain/models/payment_models.dart';

final orderDetailsProvider =
    FutureProvider.autoDispose.family<_OrderDetailsSnapshot, String>((ref, orderId) async {
  final auth = ref.watch(authControllerProvider);
  final token = auth.accessToken;
  if (token == null) {
    throw Exception('Not authenticated');
  }

  final ordersRepo = ref.read(ordersRepositoryProvider);
  final paymentsRepo = ref.read(paymentsRepositoryProvider);

  final order = await ordersRepo.fetchOrderById(orderId: orderId, bearerToken: token);

  PaymentDTO? payment;
  try {
    payment = await paymentsRepo.fetchPaymentByOrderId(orderId: orderId, bearerToken: token);
  } catch (_) {
    payment = null;
  }

  return _OrderDetailsSnapshot(order: order, payment: payment);
});

class _OrderDetailsSnapshot {
  final OrderDTO order;
  final PaymentDTO? payment;

  _OrderDetailsSnapshot({required this.order, required this.payment});
}

class OrderDetailsScreen extends ConsumerWidget {
  final String orderId;
  const OrderDetailsScreen({super.key, required this.orderId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailsAsync = ref.watch(orderDetailsProvider(orderId));

    return Scaffold(
      appBar: AppBar(title: const Text('Order details')),
      body: detailsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, st) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text(e.toString()),
          ),
        ),
        data: (snapshot) {
          final order = snapshot.order;
          final payment = snapshot.payment;

          return SafeArea(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Text(order.orderNumber, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900)),
                const SizedBox(height: 6),
                _StatusPill(status: order.status),
                const SizedBox(height: 14),
                _CardSection(
                  title: 'Timeline',
                  child: _Timeline(
                    createdAt: order.createdAt,
                    confirmedAt: order.confirmedAt,
                    status: order.status,
                  ),
                ),
                const SizedBox(height: 14),
                _CardSection(
                  title: 'Items',
                  child: Column(
                    children: order.items.map((i) {
                      return ListTile(
                        contentPadding: EdgeInsets.zero,
                        title: Text(i.ticketCategoryName.isEmpty ? i.ticketCategoryId : i.ticketCategoryName),
                        subtitle: Text('Qty: ${i.quantity}'),
                        trailing: Text(i.totalPrice.toStringAsFixed(2)),
                      );
                    }).toList(),
                  ),
                ),
                const SizedBox(height: 14),
                _CardSection(
                  title: 'Payment',
                  child: payment == null
                      ? const Text('Payment info not available yet.')
                      : Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            _PaymentSummary(payment: payment),
                            const SizedBox(height: 14),
                            if (payment.status == PaymentStatus.PROCESSING)
                              const Text(
                                'Payment is being processed…',
                                style: TextStyle(fontWeight: FontWeight.w700),
                              )
                            else if (payment.status != PaymentStatus.COMPLETED &&
                                order.status != OrderStatus.CANCELLED &&
                                order.status != OrderStatus.REFUNDED &&
                                order.status != OrderStatus.EXPIRED)
                              FilledButton.tonal(
                                onPressed: () {
                                  context.go('/app/payments/process/${order.id}');
                                },
                                child: const Text('Pay now'),
                              ),
                          ],
                        ),
                ),
                const SizedBox(height: 18),
                Row(
                  children: [
                    Expanded(
                      child: FilledButton.tonal(
                        onPressed: _canCancel(order.status)
                            ? () async {
                                final auth = ref.read(authControllerProvider);
                                if (auth.accessToken == null) return;
                                await ref.read(ordersRepositoryProvider).cancelOrder(
                                      orderId: order.id,
                                      bearerToken: auth.accessToken!,
                                    );
                                ref.invalidate(orderDetailsProvider(orderId));
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('Order canceled')),
                                );
                              }
                            : null,
                        child: const Text('Cancel order'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Reorder is not implemented yet.'),
                            ),
                          );
                        },
                        icon: const Icon(Icons.replay_outlined),
                        label: const Text('Reorder'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                OutlinedButton(
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Reorder is not implemented yet.')),
                    );
                  },
                  child: const Text('Reorder'),
                ),
                const SizedBox(height: 10),
              ],
            ),
          );
        },
      ),
    );
  }

  bool _canCancel(OrderStatus s) =>
      s != OrderStatus.COMPLETED && s != OrderStatus.CANCELLED && s != OrderStatus.REFUNDED && s != OrderStatus.EXPIRED;
}

class _StatusPill extends StatelessWidget {
  final OrderStatus status;
  const _StatusPill({required this.status});

  @override
  Widget build(BuildContext context) {
    final isCompleted = status == OrderStatus.COMPLETED;
    final isCancelled = status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED || status == OrderStatus.EXPIRED;
    final color = isCompleted ? Colors.green : isCancelled ? Colors.grey : Theme.of(context).colorScheme.primary;

    return Chip(
      label: Text(status.name),
      backgroundColor: color.withOpacity(0.15),
      labelStyle: TextStyle(color: color, fontWeight: FontWeight.w700),
    );
  }
}

class _CardSection extends StatelessWidget {
  final String title;
  final Widget child;
  const _CardSection({required this.title, required this.child});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
            const SizedBox(height: 10),
            child,
          ],
        ),
      ),
    );
  }
}

class _Timeline extends StatelessWidget {
  final DateTime createdAt;
  final DateTime? confirmedAt;
  final OrderStatus status;

  const _Timeline({
    required this.createdAt,
    required this.confirmedAt,
    required this.status,
  });

  @override
  Widget build(BuildContext context) {
    final steps = <_TimelineStep>[
      _TimelineStep(
        label: 'Placed',
        when: createdAt,
        isDone: true,
      ),
      _TimelineStep(
        label: 'Confirmed',
        when: confirmedAt,
        isDone: confirmedAt != null,
      ),
      _TimelineStep(
        label: 'Completed',
        when: status == OrderStatus.COMPLETED ? confirmedAt : null,
        isDone: status == OrderStatus.COMPLETED,
      ),
      _TimelineStep(
        label: 'Canceled',
        when: (status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED || status == OrderStatus.EXPIRED) ? confirmedAt : null,
        isDone: status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED || status == OrderStatus.EXPIRED,
      ),
    ];

    return Column(
      children: steps.map((s) {
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: Row(
            children: [
              Icon(
                s.isDone ? Icons.check_circle : Icons.radio_button_unchecked,
                color: s.isDone ? Colors.green : Theme.of(context).colorScheme.primary.withOpacity(0.4),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(s.label, style: Theme.of(context).textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w800)),
                    if (s.when != null)
                      Text(
                        s.when!.toLocal().toString(),
                        style: Theme.of(context).textTheme.bodySmall,
                      )
                    else
                      const Text('Not yet'),
                  ],
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }
}

class _TimelineStep {
  final String label;
  final DateTime? when;
  final bool isDone;

  const _TimelineStep({required this.label, required this.when, required this.isDone});
}

class _PaymentSummary extends StatelessWidget {
  final PaymentDTO payment;
  const _PaymentSummary({required this.payment});

  @override
  Widget build(BuildContext context) {
    final statusColor = switch (payment.status) {
      PaymentStatus.COMPLETED => Colors.green,
      PaymentStatus.FAILED => Colors.red,
      PaymentStatus.REFUNDED => Colors.grey,
      PaymentStatus.PROCESSING => Theme.of(context).colorScheme.primary,
    };

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Chip(
              label: Text(payment.status.name),
              backgroundColor: statusColor.withOpacity(0.15),
              labelStyle: TextStyle(color: statusColor, fontWeight: FontWeight.w700),
            ),
          ],
        ),
        const SizedBox(height: 10),
        Text('Amount: ${payment.amount.toStringAsFixed(2)} ${payment.currency}', style: Theme.of(context).textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 6),
        Text('Method: ${payment.method.name}'),
        if (payment.failureReason != null) ...[
          const SizedBox(height: 8),
          Text('Reason: ${payment.failureReason}', style: TextStyle(color: Colors.red.shade700)),
        ],
      ],
    );
  }
}

