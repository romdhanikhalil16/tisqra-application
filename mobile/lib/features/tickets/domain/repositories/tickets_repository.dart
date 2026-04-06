import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/tickets/domain/models/ticket_models.dart';

abstract class TicketsRepository {
  Future<PageResult<TicketDTO>> fetchTicketsByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  });

  Future<TicketDTO> fetchTicketById({
    required String ticketId,
    required String bearerToken,
  });
}

