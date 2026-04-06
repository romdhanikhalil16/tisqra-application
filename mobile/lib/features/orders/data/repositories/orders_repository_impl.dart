import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/orders/domain/models/order_models.dart';
import 'package:mobile/features/orders/domain/repositories/orders_repository.dart';
import 'package:mobile/core/providers/api_providers.dart';

final ordersRepositoryProvider = Provider<OrdersRepository>(
  (ref) => OrdersRepositoryImpl(
    apiClient: ref.watch(apiClientProvider),
  ),
);

class OrdersRepositoryImpl implements OrdersRepository {
  OrdersRepositoryImpl({required this.apiClient});
  final ApiClient apiClient;

  @override
  Future<PageResult<OrderDTO>> fetchOrdersByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/orders/user/$userId',
      queryParameters: {'page': page, 'size': size},
      bearerToken: bearerToken,
      dataParser: (json) {
        return PageResult.fromJson(
          json,
          itemParser: (item) => OrderDTO.fromJson(item as Map<String, dynamic>),
        );
      },
    );

    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to fetch orders');
    }

    return apiResp.data!;
  }

  @override
  Future<OrderDTO> fetchOrderById({
    required String orderId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.getApiResponse(
      '/api/orders/$orderId',
      bearerToken: bearerToken,
      dataParser: (json) => OrderDTO.fromJson(json as Map<String, dynamic>),
    );
    if (!apiResp.success || apiResp.data == null) {
      throw Exception(apiResp.error?.message ?? 'Failed to fetch order');
    }
    return apiResp.data!;
  }

  @override
  Future<void> cancelOrder({
    required String orderId,
    required String bearerToken,
  }) async {
    final apiResp = await apiClient.postApiResponseDynamic(
      '/api/orders/$orderId/cancel',
      bearerToken: bearerToken,
    );
    if (!apiResp.success) {
      throw Exception(apiResp.error?.message ?? 'Failed to cancel order');
    }
  }
}

