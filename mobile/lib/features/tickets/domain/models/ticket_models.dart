enum TicketStatus {
  ACTIVE,
  VALIDATED,
  TRANSFERRED,
  CANCELLED,
  EXPIRED,
  REFUNDED,
}

TicketStatus ticketStatusFromString(String? raw) {
  switch (raw) {
    case 'ACTIVE':
      return TicketStatus.ACTIVE;
    case 'VALIDATED':
      return TicketStatus.VALIDATED;
    case 'TRANSFERRED':
      return TicketStatus.TRANSFERRED;
    case 'CANCELLED':
      return TicketStatus.CANCELLED;
    case 'EXPIRED':
      return TicketStatus.EXPIRED;
    case 'REFUNDED':
      return TicketStatus.REFUNDED;
    default:
      return TicketStatus.ACTIVE;
  }
}

class TicketDTO {
  final String id;
  final String ticketNumber;
  final String orderId;
  final String eventId;
  final String ticketCategoryId;
  final String qrCode;
  final String ownerEmail;
  final String ownerName;
  final String? ownerUserId;
  final TicketStatus status;
  final bool isTransferable;
  final DateTime? validatedAt;
  final String? validatedBy;
  final String? scannerDeviceId;

  const TicketDTO({
    required this.id,
    required this.ticketNumber,
    required this.orderId,
    required this.eventId,
    required this.ticketCategoryId,
    required this.qrCode,
    required this.ownerEmail,
    required this.ownerName,
    required this.ownerUserId,
    required this.status,
    required this.isTransferable,
    required this.validatedAt,
    required this.validatedBy,
    required this.scannerDeviceId,
  });

  factory TicketDTO.fromJson(Map<String, dynamic> json) {
    return TicketDTO(
      id: json['id']?.toString() ?? '',
      ticketNumber: json['ticketNumber']?.toString() ?? '',
      orderId: json['orderId']?.toString() ?? '',
      eventId: json['eventId']?.toString() ?? '',
      ticketCategoryId: json['ticketCategoryId']?.toString() ?? '',
      qrCode: json['qrCode']?.toString() ?? '',
      ownerEmail: json['ownerEmail']?.toString() ?? '',
      ownerName: json['ownerName']?.toString() ?? '',
      ownerUserId: json['ownerUserId']?.toString(),
      status: ticketStatusFromString(json['status']?.toString()),
      isTransferable: json['isTransferable'] as bool? ?? true,
      validatedAt: json['validatedAt'] == null
          ? null
          : DateTime.parse(json['validatedAt'].toString()),
      validatedBy: json['validatedBy']?.toString(),
      scannerDeviceId: json['scannerDeviceId']?.toString(),
    );
  }
}

