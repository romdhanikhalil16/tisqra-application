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
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        path,
        queryParameters: queryParameters,
        options: Options(headers: _buildAuthHeaders(bearerToken)),
      );

      return ApiResponse.fromJson(
        response.data ?? const <String, dynamic>{},
        dataParser: dataParser,
      );
    } on DioException catch (e) {
      return _handleDioError<T>(e, dataParser: dataParser);
    }
  }

  Future<ApiResponse<T>> postApiResponse<T>(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
    required T Function(dynamic json) dataParser,
  }) async {
    try {
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
    } on DioException catch (e) {
      return _handleDioError<T>(e, dataParser: dataParser);
    }
  }

  Future<ApiResponse<dynamic>> postApiResponseDynamic(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        path,
        data: body,
        queryParameters: queryParameters,
        options: Options(headers: _buildAuthHeaders(bearerToken)),
      );

      return ApiResponse.fromJson(response.data ?? const <String, dynamic>{});
    } on DioException catch (e) {
      return _handleDioError<dynamic>(e);
    }
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
    try {
      final response = await _dio.delete<Map<String, dynamic>>(
        path,
        queryParameters: queryParameters,
        options: Options(headers: _buildAuthHeaders(bearerToken)),
      );

      return ApiResponse.fromJson(
        response.data ?? const <String, dynamic>{},
      );
    } on DioException catch (e) {
      return _handleDioError<dynamic>(e);
    }
  }

  Future<ApiResponse<dynamic>> putApiResponseDynamic(
    String path, {
    Object? body,
    Map<String, dynamic>? queryParameters,
    String? bearerToken,
  }) async {
    try {
      final response = await _dio.put<Map<String, dynamic>>(
        path,
        data: body,
        queryParameters: queryParameters,
        options: Options(headers: _buildAuthHeaders(bearerToken)),
      );

      return ApiResponse.fromJson(
        response.data ?? const <String, dynamic>{},
      );
    } on DioException catch (e) {
      return _handleDioError<dynamic>(e);
    }
  }

  ApiResponse<T> _handleDioError<T>(DioException e, {T Function(dynamic)? dataParser}) {
    if (e.response?.data != null && e.response!.data is Map<String, dynamic>) {
      try {
        return ApiResponse.fromJson(
          e.response!.data as Map<String, dynamic>,
          dataParser: dataParser,
        );
      } catch (_) {}
    }
    
    // Provide a more human readable error for connection refused/timeout
    String msg = e.message ?? 'Unknown error';
    if (e.type == DioExceptionType.connectionError || e.type == DioExceptionType.connectionTimeout) {
      msg = 'Unable to connect to server. Please check your internet connection or server IP.';
    }

    return ApiResponse(
      success: false,
      data: null,
      error: ApiErrorResponse(
        code: e.response?.statusCode?.toString() ?? 'NETWORK_ERROR',
        message: msg,
        details: null,
      ),
    );
  }
}

