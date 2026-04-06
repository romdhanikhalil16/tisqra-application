enum OrderStatus {
  PENDING,
  CONFIRMED,
  PROCESSING,
  COMPLETED,
  CANCELLED,
  REFUNDED,
  EXPIRED,
}

OrderStatus orderStatusFromString(String? raw) {
  switch (raw) {
    case 'PENDING':
      return OrderStatus.PENDING;
    case 'CONFIRMED':
      return OrderStatus.CONFIRMED;
    case 'PROCESSING':
      return OrderStatus.PROCESSING;
    case 'COMPLETED':
      return OrderStatus.COMPLETED;
    case 'CANCELLED':
      return OrderStatus.CANCELLED;
    case 'REFUNDED':
      return OrderStatus.REFUNDED;
    case 'EXPIRED':
      return OrderStatus.EXPIRED;
    default:
      return OrderStatus.PENDING;
  }
}

class OrderItemDTO {
  final String ticketCategoryId;
  final String ticketCategoryName;
  final int quantity;
  final double unitPrice;
  final double totalPrice;

  const OrderItemDTO({
    required this.ticketCategoryId,
    required this.ticketCategoryName,
    required this.quantity,
    required this.unitPrice,
    required this.totalPrice,
  });

  factory OrderItemDTO.fromJson(Map<String, dynamic> json) {
    return OrderItemDTO(
      ticketCategoryId: json['ticketCategoryId']?.toString() ?? '',
      ticketCategoryName: json['ticketCategoryName']?.toString() ?? '',
      quantity: (json['quantity'] as num?)?.toInt() ?? 0,
      unitPrice: (json['unitPrice'] as num?)?.toDouble() ?? 0,
      totalPrice: (json['totalPrice'] as num?)?.toDouble() ?? 0,
    );
  }
}

class OrderDTO {
  final String id;
  final String orderNumber;
  final String userId;
  final String eventId;
  final OrderStatus status;
  final double subtotal;
  final double discountAmount;
  final double totalAmount;
  final String currency;
  final String? promoCode;
  final List<OrderItemDTO> items;
  final int totalTickets;
  final DateTime createdAt;
  final DateTime? confirmedAt;

  const OrderDTO({
    required this.id,
    required this.orderNumber,
    required this.userId,
    required this.eventId,
    required this.status,
    required this.subtotal,
    required this.discountAmount,
    required this.totalAmount,
    required this.currency,
    this.promoCode,
    required this.items,
    required this.totalTickets,
    required this.createdAt,
    required this.confirmedAt,
  });

  factory OrderDTO.fromJson(Map<String, dynamic> json) {
    final itemsJson = (json['items'] as List<dynamic>? ?? const []);
    return OrderDTO(
      id: json['id']?.toString() ?? '',
      orderNumber: json['orderNumber']?.toString() ?? '',
      userId: json['userId']?.toString() ?? '',
      eventId: json['eventId']?.toString() ?? '',
      status: orderStatusFromString(json['status']?.toString()),
      subtotal: (json['subtotal'] as num?)?.toDouble() ?? 0,
      discountAmount: (json['discountAmount'] as num?)?.toDouble() ?? 0,
      totalAmount: (json['totalAmount'] as num?)?.toDouble() ?? 0,
      currency: json['currency']?.toString() ?? 'USD',
      promoCode: json['promoCode']?.toString(),
      items: itemsJson.map((e) => OrderItemDTO.fromJson(e as Map<String, dynamic>)).toList(),
      totalTickets: (json['totalTickets'] as num?)?.toInt() ?? 0,
      createdAt: DateTime.parse(json['createdAt']?.toString() ?? DateTime.now().toIso8601String()),
      confirmedAt: json['confirmedAt'] == null ? null : DateTime.parse(json['confirmedAt'].toString()),
    );
  }
}

