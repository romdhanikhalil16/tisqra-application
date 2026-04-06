import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class PaymentFailureScreen extends StatelessWidget {
  final String orderId;
  final String? reason;
  const PaymentFailureScreen({
    super.key,
    required this.orderId,
    this.reason,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Payment')),
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.error_outline, size: 90, color: Colors.red.shade700),
                const SizedBox(height: 14),
                Text('Payment failed', style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 8),
                Text(
                  reason ?? 'Please try again or choose a different payment method.',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 18),
                FilledButton.tonal(
                  onPressed: () => context.go('/app/payments/process/$orderId'),
                  child: const Text('Try again'),
                ),
                const SizedBox(height: 10),
                OutlinedButton(
                  onPressed: () => context.go('/app/orders'),
                  child: const Text('Back to orders'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

