enum PaymentMethod {
  CARD,
  MOBILE_MONEY,
  BANK_TRANSFER,
  PAYPAL,
  WALLET,
}

PaymentMethod paymentMethodFromString(String? raw) {
  switch (raw) {
    case 'MOBILE_MONEY':
      return PaymentMethod.MOBILE_MONEY;
    case 'BANK_TRANSFER':
      return PaymentMethod.BANK_TRANSFER;
    case 'PAYPAL':
      return PaymentMethod.PAYPAL;
    case 'WALLET':
      return PaymentMethod.WALLET;
    case 'CARD':
    default:
      return PaymentMethod.CARD;
  }
}

enum PaymentStatus {
  PROCESSING,
  COMPLETED,
  FAILED,
  REFUNDED,
}

PaymentStatus paymentStatusFromString(String? raw) {
  switch (raw) {
    case 'COMPLETED':
      return PaymentStatus.COMPLETED;
    case 'FAILED':
      return PaymentStatus.FAILED;
    case 'REFUNDED':
      return PaymentStatus.REFUNDED;
    case 'PROCESSING':
    default:
      return PaymentStatus.PROCESSING;
  }
}

class PaymentDTO {
  final String id;
  final String orderId;
  final double amount;
  final String currency;
  final PaymentMethod method;
  final String? provider;
  final String? providerPaymentId;
  final PaymentStatus status;
  final String? failureReason;
  final DateTime? paidAt;
  final DateTime createdAt;

  const PaymentDTO({
    required this.id,
    required this.orderId,
    required this.amount,
    required this.currency,
    required this.method,
    this.provider,
    this.providerPaymentId,
    required this.status,
    this.failureReason,
    required this.paidAt,
    required this.createdAt,
  });

  factory PaymentDTO.fromJson(Map<String, dynamic> json) {
    return PaymentDTO(
      id: json['id']?.toString() ?? '',
      orderId: json['orderId']?.toString() ?? '',
      amount: (json['amount'] as num?)?.toDouble() ?? 0,
      currency: json['currency']?.toString() ?? 'USD',
      method: paymentMethodFromString(json['method']?.toString()),
      provider: json['provider']?.toString(),
      providerPaymentId: json['providerPaymentId']?.toString(),
      status: paymentStatusFromString(json['status']?.toString()),
      failureReason: json['failureReason']?.toString(),
      paidAt: json['paidAt'] == null ? null : DateTime.parse(json['paidAt'].toString()),
      createdAt: DateTime.parse(json['createdAt']?.toString() ?? DateTime.now().toIso8601String()),
    );
  }
}

