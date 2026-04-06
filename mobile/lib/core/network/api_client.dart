import 'package:dio/dio.dart';
import 'package:mobile/core/network/api_response.dart';

class ApiClient {
  ApiClient({
    required String baseUrl,
    required Dio dio,
  })  : _dio = dio,
        _baseUrl = baseUrl;

  final Dio _dio;
  final String _baseUrl;

  static BaseOptions _baseOptions(String baseUrl) {
    return BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
      headers: const {'Content-Type': 'application/json'},
    );
  }

  factory ApiClient.create(String baseUrl) {
    final dio = Dio(_baseOptions(baseUrl));
    return ApiClient(baseUrl: baseUrl, dio: dio);
  }

  Future<ApiResponse<T>> getApiResponse<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
    required T Function(dynamic json) dataParser,
  }) async {
    final response = await _dio.get<Map<String, dynamic>>(
      path,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );

    return ApiResponse.fromJson(
      response.data ?? const <String, dynamic>{},
      dataParser: dataParser,
    );
  }

  Future<ApiResponse<T>> postApiResponse<T>(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
    required T Function(dynamic json) dataParser,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      path,
      data: body,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );

    return ApiResponse.fromJson(
      response.data ?? const <String, dynamic>{},
      dataParser: dataParser,
    );
  }

  Future<ApiResponse<dynamic>> postApiResponseDynamic(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      path,
      data: body,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );

    return ApiResponse.fromJson(response.data ?? const <String, dynamic>{});
  }

  Future<Map<String, dynamic>> getJson(
    String path, {
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    final response = await _dio.get<Map<String, dynamic>>(
      path,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );
    return response.data ?? const <String, dynamic>{};
  }

  Map<String, dynamic> _buildAuthHeaders(String? bearerToken) {
    if (bearerToken == null || bearerToken.isEmpty) {
      return const <String, dynamic>{};
    }
    return <String, dynamic>{'Authorization': 'Bearer $bearerToken'};
  }

  Future<ApiResponse<dynamic>> deleteApiResponseDynamic(
    String path, {
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    final response = await _dio.delete<Map<String, dynamic>>(
      path,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );

    return ApiResponse.fromJson(
      response.data ?? const <String, dynamic>{},
    );
  }

  Future<ApiResponse<dynamic>> putApiResponseDynamic(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    final response = await _dio.put<Map<String, dynamic>>(
      path,
      data: body,
      queryParameters: queryParameters,
      options: Options(headers: _buildAuthHeaders(bearerToken)),
    );

    return ApiResponse.fromJson(
      response.data ?? const <String, dynamic>{},
    );
  }
}

