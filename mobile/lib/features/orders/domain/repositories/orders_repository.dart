import 'package:mobile/core/network/page_result.dart';
import 'package:mobile/features/orders/domain/models/order_models.dart';

abstract class OrdersRepository {
  Future<PageResult<OrderDTO>> fetchOrdersByUser({
    required String userId,
    required int page,
    required int size,
    required String bearerToken,
  });

  Future<OrderDTO> fetchOrderById({
    required String orderId,
    required String bearerToken,
  });

  Future<void> cancelOrder({
    required String orderId,
    required String bearerToken,
  });
}

