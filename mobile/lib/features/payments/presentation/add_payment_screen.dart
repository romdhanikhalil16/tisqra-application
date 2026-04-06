import 'package:flutter/material.dart';
import 'package:mobile/features/payments/data/local/saved_cards_store.dart';
import 'package:mobile/shared/widgets/primary_button.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:go_router/go_router.dart';

class AddPaymentScreen extends StatefulWidget {
  const AddPaymentScreen({super.key});

  @override
  State<AddPaymentScreen> createState() => _AddPaymentScreenState();
}

class _AddPaymentScreenState extends State<AddPaymentScreen> {
  final _cardNumber = TextEditingController();
  final _cardHolderName = TextEditingController();
  final _expiryMonth = TextEditingController();
  final _expiryYear = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _cardNumber.dispose();
    _cardHolderName.dispose();
    _expiryMonth.dispose();
    _expiryYear.dispose();
    super.dispose();
  }

  String? _validateCardNumber(String v) {
    final digits = v.replaceAll(RegExp(r'\D'), '');
    if (digits.length < 12) return 'Enter a valid card number';
    if (digits.length > 19) return 'Card number looks too long';
    return null;
  }

  String? _validateExpiryMonth(String v) {
    final m = int.tryParse(v);
    if (m == null) return 'Month must be a number';
    if (m < 1 || m > 12) return 'Month must be between 01 and 12';
    return null;
  }

  String? _validateExpiryYear(String v) {
    final y = int.tryParse(v);
    if (y == null) return 'Year must be a number';
    final year = v.length == 2 ? (2000 + y) : y;
    if (year < DateTime.now().year) return 'Card looks expired';
    return null;
  }

  Future<void> _onSave() async {
    final cardNumberErr = _validateCardNumber(_cardNumber.text);
    final monthErr = _validateExpiryMonth(_expiryMonth.text.trim());
    final yearErr = _validateExpiryYear(_expiryYear.text.trim());

    if (cardNumberErr != null) return _showErr(cardNumberErr);
    if (monthErr != null) return _showErr(monthErr);
    if (yearErr != null) return _showErr(yearErr);

    final cardNumber = _cardNumber.text.replaceAll(RegExp(r'\s+'), '');
    final holderName = _cardHolderName.text.trim();
    if (holderName.length < 2) return _showErr('Enter card holder name');

    final month = _expiryMonth.text.trim().padLeft(2, '0');
    final yearRaw = _expiryYear.text.trim();
    final year = yearRaw.length == 2
        ? yearRaw
        : (int.parse(yearRaw) % 100).toString().padLeft(2, '0');

    setState(() => _loading = true);
    try {
      final id = DateTime.now().microsecondsSinceEpoch.toString();
      await SavedCardsStore().addCard(
        SavedCard(
          id: id,
          cardNumber: cardNumber,
          cardHolderName: holderName,
          expiryMonth: month,
          expiryYear: year,
        ),
      );
      if (!mounted) return;
      context.pop();
    } catch (e) {
      if (!mounted) return;
      _showErr(e.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _showErr(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg), backgroundColor: Colors.red),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Add payment method')),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: ListView(
            children: [
              const Text(
                'Add a card',
                style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900),
              ),
              const SizedBox(height: 8),
              Text(
                'This is stored locally for quick checkout (demo).',
                style: Theme.of(context).textTheme.bodySmall,
              ),
              const SizedBox(height: 18),
              AppTextField(
                controller: _cardNumber,
                label: 'Card number',
                hint: '1234 5678 9012 3456',
                keyboardType: TextInputType.number,
                textInputAction: TextInputAction.next,
              ),
              const SizedBox(height: 12),
              AppTextField(
                controller: _cardHolderName,
                label: 'Card holder name',
                hint: 'John Doe',
                textInputAction: TextInputAction.next,
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: AppTextField(
                      controller: _expiryMonth,
                      label: 'Expiry month',
                      hint: 'MM',
                      keyboardType: TextInputType.number,
                      textInputAction: TextInputAction.next,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: AppTextField(
                      controller: _expiryYear,
                      label: 'Expiry year',
                      hint: 'YY or YYYY',
                      keyboardType: TextInputType.number,
                      textInputAction: TextInputAction.done,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 18),
              PrimaryButton(
                label: 'Save card',
                isLoading: _loading,
                onPressed: _onSave,
                icon: Icons.check_outlined,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

