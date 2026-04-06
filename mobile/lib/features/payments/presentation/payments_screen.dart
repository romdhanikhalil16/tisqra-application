import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/features/payments/data/local/saved_cards_store.dart';

final savedCardsProvider = FutureProvider.autoDispose<List<SavedCard>>((ref) async {
  return SavedCardsStore().loadCards();
});

class PaymentsScreen extends ConsumerWidget {
  const PaymentsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cardsAsync = ref.watch(savedCardsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Payment methods')),
      body: cardsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, st) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Failed to load cards', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 10),
                Text(err.toString()),
                const SizedBox(height: 16),
                FilledButton.tonal(
                  onPressed: () => ref.invalidate(savedCardsProvider),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
        data: (cards) {
          if (cards.isEmpty) return const _EmptyPaymentsState();
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: cards.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final c = cards[index];
              return Card(
                elevation: 0,
                child: ListTile(
                  leading: const CircleAvatar(child: Icon(Icons.credit_card)),
                  title: Text(c.maskNumber()),
                  subtitle: Text('Expires: ${c.expiryMonth}/${c.expiryYear}'),
                  trailing: IconButton(
                    icon: const Icon(Icons.delete_outline),
                    onPressed: () async {
                      await SavedCardsStore().deleteCard(c.id);
                      ref.invalidate(savedCardsProvider);
                    },
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.go('/app/payments/add'),
        icon: const Icon(Icons.add),
        label: const Text('Add card'),
      ),
    );
  }
}

class _EmptyPaymentsState extends StatelessWidget {
  const _EmptyPaymentsState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.credit_card_outlined, size: 68, color: Theme.of(context).colorScheme.primary.withOpacity(0.7)),
            const SizedBox(height: 10),
            Text('No saved cards', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(
              'Add a card to speed up checkout and ticket purchases.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => context.go('/app/payments/add'),
              child: const Text('Add your first card'),
            )
          ],
        ),
      ),
    );
  }
}

