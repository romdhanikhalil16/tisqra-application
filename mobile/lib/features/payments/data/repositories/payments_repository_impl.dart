import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/features/payments/domain/models/payment_models.dart';
import 'package:mobile/features/payments/domain/repositories/payments_repository.dart';
import 'package:mobile/core/providers/api_providers.dart';

final paymentsRepositoryProvider = Provider<PaymentsRepository>(
  (ref) => PaymentsRepositoryImpl(
    apiClient: ref.watch(apiClientProvider),
  ),
);

class PaymentsRepositoryImpl implements PaymentsRepository {
  PaymentsRepositoryImpl({required this.apiClient});
  final ApiClient apiClient;

  String _methodToBackend(PaymentMethod m) => switch (m) {
        PaymentMethod.CARD => 'CARD',
        PaymentMethod.MOBILE_MONEY => 'MOBILE_MONEY',
        PaymentMethod.BANK_TRANSFER => 'BANK_TRANSFER',
        PaymentMethod.PAYPAL => 'PAYPAL',
        PaymentMethod.WALLET => 'WALLET',
      };

  @override
  Future<PaymentDTO> fetchPaymentByOrderId({
    required String orderId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/payments/order/$orderId',
      bearerToken: bearerToken,
      dataParser: (json) => json as Map<String, dynamic>,
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to fetch payment');
    }

    return PaymentDTO.fromJson(apiResp.data!);
  }

  @override
  Future<PaymentDTO> processPayment({
    required String orderId,
    required PaymentMethod method,
    required String bearerToken,
    String? cardNumber,
    String? cardHolderName,
    String? expiryMonth,
    String? expiryYear,
    String? cvv,
  }) async {
    final apiResp = await apiClient.postApiResponseDynamic(
      '/api/payments/process',
      bearerToken: bearerToken,
      body: {
        'orderId': orderId,
        'paymentMethod': _methodToBackend(method),
        'cardNumber': cardNumber,
        'cardHolderName': cardHolderName,
        'expiryMonth': expiryMonth,
        'expiryYear': expiryYear,
        'cvv': cvv,
      },
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Payment failed');
    }

    return PaymentDTO.fromJson(apiResp.data! as Map<String, dynamic>);
  }
}

