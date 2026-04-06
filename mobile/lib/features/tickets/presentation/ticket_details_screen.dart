import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/tickets/data/repositories/tickets_repository_impl.dart';
import 'package:mobile/features/tickets/domain/models/ticket_models.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:share_plus/share_plus.dart';

final ticketDetailsProvider = FutureProvider.autoDispose.family<TicketDTO, String>(
  (ref, ticketId) async {
    final auth = ref.watch(authControllerProvider);
    if (auth.accessToken == null) {
      throw Exception('Not authenticated');
    }
    final repo = ref.read(ticketsRepositoryProvider);
    return repo.fetchTicketById(ticketId: ticketId, bearerToken: auth.accessToken!);
  },
);

class TicketDetailsScreen extends ConsumerWidget {
  final String ticketId;
  const TicketDetailsScreen({super.key, required this.ticketId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ticketAsync = ref.watch(ticketDetailsProvider(ticketId));
    return Scaffold(
      appBar: AppBar(title: const Text('Ticket')),
      body: ticketAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, st) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Failed to load ticket', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 10),
                Text(e.toString()),
                const SizedBox(height: 16),
                FilledButton.tonal(
                  onPressed: () => ref.invalidate(ticketDetailsProvider(ticketId)),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
        data: (t) {
          return SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: ListView(
                children: [
                  Center(
                    child: QrImageView(
                      data: t.qrCode,
                      version: QrVersions.auto,
                      size: 220,
                      backgroundColor: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _MetaCard(
                    children: [
                      _MetaRow(label: 'Ticket number', value: t.ticketNumber),
                      _MetaRow(label: 'Status', value: t.status.name),
                      _MetaRow(label: 'Event', value: t.eventId),
                      _MetaRow(label: 'Ticket category', value: t.ticketCategoryId),
                    ],
                  ),
                  const SizedBox(height: 14),
                  if (t.validatedAt != null)
                    _MetaCard(
                      children: [
                        _MetaRow(
                          label: 'Validated at',
                          value: t.validatedAt.toString(),
                        ),
                        if (t.validatedBy != null)
                          _MetaRow(label: 'Validated by', value: t.validatedBy!),
                      ],
                    ),
                  const SizedBox(height: 18),
                  Row(
                    children: [
                      Expanded(
                        child: FilledButton.icon(
                          onPressed: () {
                            Share.share('Ticket ${t.ticketNumber} - QR: ${t.qrCode}');
                          },
                          icon: const Icon(Icons.share_outlined),
                          label: const Text('Share'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () {
                            // For now, share/print works well; real "download QR image" can be added later.
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Download is not implemented yet.')),
                            );
                          },
                          icon: const Icon(Icons.download_outlined),
                          label: const Text('Download'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'Show this QR code at the entrance. Keep it safe.',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _MetaCard extends StatelessWidget {
  final List<Widget> children;
  const _MetaCard({required this.children});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: children,
        ),
      ),
    );
  }
}

class _MetaRow extends StatelessWidget {
  final String label;
  final String value;
  const _MetaRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 150,
            child: Text(label, style: Theme.of(context).textTheme.bodySmall),
          ),
          Expanded(
            child: Text(value, style: Theme.of(context).textTheme.bodyMedium),
          ),
        ],
      ),
    );
  }
}

