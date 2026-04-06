import 'package:mobile/features/payments/domain/models/payment_models.dart';

abstract class PaymentsRepository {
  Future<PaymentDTO> fetchPaymentByOrderId({
    required String orderId,
    required String bearerToken,
  });

  Future<PaymentDTO> processPayment({
    required String orderId,
    required PaymentMethod method,
    required String bearerToken,
    String? cardNumber,
    String? cardHolderName,
    String? expiryMonth,
    String? expiryYear,
    String? cvv,
  });
}

