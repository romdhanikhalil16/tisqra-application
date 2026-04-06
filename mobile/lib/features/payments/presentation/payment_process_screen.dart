import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/payments/data/local/saved_cards_store.dart';
import 'package:mobile/features/payments/data/repositories/payments_repository_impl.dart';
import 'package:mobile/features/payments/domain/models/payment_models.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:mobile/shared/widgets/primary_button.dart';

class PaymentProcessScreen extends ConsumerStatefulWidget {
  final String orderId;
  const PaymentProcessScreen({super.key, required this.orderId});

  @override
  ConsumerState<PaymentProcessScreen> createState() => _PaymentProcessScreenState();
}

class _PaymentProcessScreenState extends ConsumerState<PaymentProcessScreen> {
  final _cvvCtrl = TextEditingController();
  List<SavedCard> _cards = const [];
  SavedCard? _selected;
  bool _loading = false;
  bool _cardsLoading = true;

  @override
  void initState() {
    super.initState();
    _loadCards();
  }

  Future<void> _loadCards() async {
    final cards = await SavedCardsStore().loadCards();
    if (!mounted) return;
    setState(() {
      _cards = cards;
      _selected = cards.isNotEmpty ? cards.first : null;
      _cardsLoading = false;
    });
  }

  @override
  void dispose() {
    _cvvCtrl.dispose();
    super.dispose();
  }

  String? _validateCvv(String v) {
    final digits = v.replaceAll(RegExp(r'\D'), '');
    if (digits.length < 3) return 'CVV must be at least 3 digits';
    if (digits.length > 4) return 'CVV looks too long';
    return null;
  }

  Future<void> _onPay() async {
    final auth = ref.read(authControllerProvider);
    final token = auth.accessToken;
    if (token == null) {
      context.go('/login');
      return;
    }

    if (_selected == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Add/select a payment card first')),
      );
      context.go('/app/payments/add');
      return;
    }

    final cvvErr = _validateCvv(_cvvCtrl.text);
    if (cvvErr != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(cvvErr), backgroundColor: Colors.red),
      );
      return;
    }

    setState(() => _loading = true);
    try {
      final paymentsRepo = ref.read(paymentsRepositoryProvider);
      await paymentsRepo.processPayment(
        orderId: widget.orderId,
        method: PaymentMethod.CARD,
        bearerToken: token,
        cardNumber: _selected!.cardNumber,
        cardHolderName: _selected!.cardHolderName,
        expiryMonth: _selected!.expiryMonth,
        expiryYear: _selected!.expiryYear,
        cvv: _cvvCtrl.text,
      );
      if (!mounted) return;
      context.go('/app/payments/success/${widget.orderId}');
    } catch (e) {
      if (!mounted) return;
      context.go('/app/payments/failure/${widget.orderId}', extra: e.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Process payment')),
      body: SafeArea(
        child: _cardsLoading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  Text(
                    'Order: ${widget.orderId}',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 14),
                  const Text('Payment card', style: TextStyle(fontWeight: FontWeight.w800)),
                  const SizedBox(height: 10),
                  if (_cards.isEmpty)
                    _EmptyCardsInline(
                      onAdd: () => context.go('/app/payments/add'),
                    )
                  else
                    ..._cards.map((c) {
                      final selected = _selected?.id == c.id;
                      return Card(
                        elevation: 0,
                        child: RadioListTile<SavedCard>(
                          value: c,
                          groupValue: _selected,
                          selected: selected,
                          onChanged: (v) {
                            setState(() => _selected = v);
                          },
                          title: Text(c.maskNumber()),
                          subtitle: Text('Expires: ${c.expiryMonth}/${c.expiryYear}'),
                        ),
                      );
                    }),
                  const SizedBox(height: 14),
                  AppTextField(
                    controller: _cvvCtrl,
                    label: 'CVV',
                    hint: '123',
                    keyboardType: TextInputType.number,
                    textInputAction: TextInputAction.done,
                    onChanged: (_) => setState(() {}),
                  ),
                  const SizedBox(height: 18),
                  PrimaryButton(
                    label: _loading ? 'Processing…' : 'Pay now',
                    isLoading: _loading,
                    onPressed: _onPay,
                    icon: Icons.payment_outlined,
                  ),
                ],
              ),
      ),
    );
  }
}

class _EmptyCardsInline extends StatelessWidget {
  final VoidCallback onAdd;
  const _EmptyCardsInline({required this.onAdd});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('No saved cards found.', style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 8),
          FilledButton.tonal(
            onPressed: onAdd,
            child: const Text('Add a card'),
          )
        ],
      ),
    );
  }
}

