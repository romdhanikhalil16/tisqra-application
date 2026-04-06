import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/tickets/domain/models/ticket_models.dart';
import 'package:mobile/features/tickets/domain/repositories/tickets_repository.dart';
import 'package:mobile/core/providers/api_providers.dart';

final ticketsRepositoryProvider = Provider<TicketsRepository>(
  (ref) => TicketsRepositoryImpl(
    apiClient: ref.watch(apiClientProvider),
  ),
);

class TicketsRepositoryImpl implements TicketsRepository {
  TicketsRepositoryImpl({required this.apiClient});
  final ApiClient apiClient;

  @override
  Future<PageResult<TicketDTO>> fetchTicketsByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/tickets/user/$userId',
      queryParameters: {'page': page, 'size': size},
      bearerToken: bearerToken,
      dataParser: (json) {
        return PageResult.fromJson(
          json,
          itemParser: (item) =>
              TicketDTO.fromJson(item as Map<String, dynamic>),
        );
      },
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to fetch tickets');
    }

    return apiResp.data!;
  }

  @override
  Future<TicketDTO> fetchTicketById({
    required String ticketId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/tickets/$ticketId',
      bearerToken: bearerToken,
      dataParser: (json) => TicketDTO.fromJson(json as Map<String, dynamic>),
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to fetch ticket');
    }
    return apiResp.data!;
  }
}

