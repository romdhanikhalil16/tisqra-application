enum NotificationChannel {
  EMAIL,
  PUSH,
  SMS,
  IN_APP,
}

NotificationChannel notificationChannelFromString(String? raw) {
  switch (raw) {
    case 'EMAIL':
      return NotificationChannel.EMAIL;
    case 'PUSH':
      return NotificationChannel.PUSH;
    case 'SMS':
      return NotificationChannel.SMS;
    case 'IN_APP':
      return NotificationChannel.IN_APP;
    default:
      return NotificationChannel.IN_APP;
  }
}

enum NotificationType {
  ACCOUNT_VERIFICATION,
  PASSWORD_RESET,
  TICKET_PURCHASE,
  TICKET_TRANSFER,
  PAYMENT_RECEIPT,
  EVENT_REMINDER,
  TICKET_VALIDATION,
  PAYMENT_STATUS,
  ORDER_CONFIRMATION,
  REFUND_CONFIRMATION,
}

NotificationType notificationTypeFromString(String? raw) {
  switch (raw) {
    case 'ACCOUNT_VERIFICATION':
      return NotificationType.ACCOUNT_VERIFICATION;
    case 'PASSWORD_RESET':
      return NotificationType.PASSWORD_RESET;
    case 'TICKET_PURCHASE':
      return NotificationType.TICKET_PURCHASE;
    case 'TICKET_TRANSFER':
      return NotificationType.TICKET_TRANSFER;
    case 'PAYMENT_RECEIPT':
      return NotificationType.PAYMENT_RECEIPT;
    case 'EVENT_REMINDER':
      return NotificationType.EVENT_REMINDER;
    case 'TICKET_VALIDATION':
      return NotificationType.TICKET_VALIDATION;
    case 'PAYMENT_STATUS':
      return NotificationType.PAYMENT_STATUS;
    case 'ORDER_CONFIRMATION':
      return NotificationType.ORDER_CONFIRMATION;
    case 'REFUND_CONFIRMATION':
      return NotificationType.REFUND_CONFIRMATION;
    default:
      return NotificationType.ORDER_CONFIRMATION;
  }
}

class NotificationDTO {
  final String id;
  final String userId;
  final NotificationType type;
  final NotificationChannel channel;
  final String? recipient;
  final String? subject;
  final String? content;
  final bool sent;
  final bool read;
  final DateTime? sentAt;
  final String? errorMessage;
  final DateTime createdAt;

  const NotificationDTO({
    required this.id,
    required this.userId,
    required this.type,
    required this.channel,
    required this.recipient,
    required this.subject,
    required this.content,
    required this.sent,
    required this.read,
    required this.sentAt,
    required this.errorMessage,
    required this.createdAt,
  });

  factory NotificationDTO.fromJson(Map<String, dynamic> json) {
    return NotificationDTO(
      id: json['id']?.toString() ?? '',
      userId: json['userId']?.toString() ?? '',
      type: notificationTypeFromString(json['type']?.toString()),
      channel: notificationChannelFromString(json['channel']?.toString()),
      recipient: json['recipient']?.toString(),
      subject: json['subject']?.toString(),
      content: json['content']?.toString(),
      sent: json['sent'] as bool? ?? false,
      read: json['read'] as bool? ?? false,
      sentAt: json['sentAt'] == null ? null : DateTime.parse(json['sentAt'].toString()),
      errorMessage: json['errorMessage']?.toString(),
      createdAt: DateTime.parse(json['createdAt']?.toString() ?? DateTime.now().toIso8601String()),
    );
  }
}

