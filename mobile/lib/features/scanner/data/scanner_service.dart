import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/api_response.dart';
import 'package:mobile/core/providers/api_providers.dart';

final scannerServiceProvider = Provider<ScannerService>((ref) {
  final apiClient = ref.read(apiClientProvider);
  return ScannerService(apiClient);
});

class ScannerService {
  final ApiClient _apiClient;

  ScannerService(this._apiClient);

  Future<ApiResponse<dynamic>> validateTicket(
    String token, {
    required String qrCode,
    required String scannerId,
    required String scannerName,
  }) {
    return _apiClient.postApiResponseDynamic(
      '/api/tickets/validate',
      bearerToken: token,
      queryParameters: {
        'qrCode': qrCode,
        'scannerId': scannerId,
        'scannerName': scannerName,
      },
    );
  }
}
