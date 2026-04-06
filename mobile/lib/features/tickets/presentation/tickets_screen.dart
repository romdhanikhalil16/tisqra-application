import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/tickets/data/repositories/tickets_repository_impl.dart';
import 'package:mobile/features/tickets/domain/models/ticket_models.dart';
import 'package:qr_flutter/qr_flutter.dart';

final ticketsPageProvider = FutureProvider.autoDispose<List<TicketDTO>>((ref) async {
  final auth = ref.watch(authControllerProvider);
  final userId = auth.userId;
  final token = auth.accessToken;
  if (userId == null || token == null) return const [];

  final repo = ref.read(ticketsRepositoryProvider);
  final page = await repo.fetchTicketsByUser(
    userId: userId,
    page: 0,
    size: 50,
    bearerToken: token,
  );
  return page.content;
});

class TicketsScreen extends ConsumerWidget {
  const TicketsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ticketsAsync = ref.watch(ticketsPageProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Tickets')),
      body: ticketsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, st) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Failed to load tickets', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 10),
                Text(err.toString()),
                const SizedBox(height: 16),
                FilledButton.tonal(
                  onPressed: () => ref.invalidate(ticketsPageProvider),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
        data: (tickets) {
          if (tickets.isEmpty) {
            return const _EmptyTicketsState();
          }

          final sorted = [...tickets]..sort((a, b) => (b.validatedAt ?? DateTime.fromMillisecondsSinceEpoch(0)).compareTo(a.validatedAt ?? DateTime.fromMillisecondsSinceEpoch(0)));

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: sorted.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final t = sorted[index];
              final when = t.validatedAt ?? DateTime.now();
              return Card(
                elevation: 0,
                child: ListTile(
                  contentPadding: const EdgeInsets.all(14),
                  leading: ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: SizedBox(
                      width: 54,
                      height: 54,
                      child: QrImageView(
                        data: t.qrCode,
                        version: QrVersions.auto,
                        size: 54,
                        backgroundColor: Colors.white,
                      ),
                    ),
                  ),
                  title: Text(
                    t.ticketNumber,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  subtitle: Text(
                    '${t.eventId} • ${_statusLabel(t.status)}',
                    maxLines: 2,
                  ),
                  trailing: _TicketStatusChip(status: t.status),
                  onTap: () => context.go('/app/tickets/${t.id}'),
                ),
              );
            },
          );
        },
      ),
    );
  }

  String _statusLabel(TicketStatus status) => status.name;
}

class _TicketStatusChip extends StatelessWidget {
  final TicketStatus status;
  const _TicketStatusChip({required this.status});

  @override
  Widget build(BuildContext context) {
    final color = switch (status) {
      TicketStatus.VALIDATED || TicketStatus.TRANSFERRED => Colors.green,
      TicketStatus.CANCELLED || TicketStatus.EXPIRED || TicketStatus.REFUNDED => Colors.grey,
      TicketStatus.ACTIVE => Theme.of(context).colorScheme.primary,
    };
    return Chip(
      label: Text(status.name),
      backgroundColor: color.withOpacity(0.15),
      labelStyle: TextStyle(color: color),
    );
  }
}

class _EmptyTicketsState extends StatelessWidget {
  const _EmptyTicketsState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.confirmation_num_outlined, size: 64, color: Theme.of(context).colorScheme.primary.withOpacity(0.7)),
            const SizedBox(height: 10),
            Text('No tickets yet', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(
              'When you purchase tickets, they will appear here with their QR codes.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

