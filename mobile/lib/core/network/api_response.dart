class ApiErrorResponse {
  final String? code;
  final String? message;
  final dynamic details;

  ApiErrorResponse({
    required this.code,
    required this.message,
    required this.details,
  });

  factory ApiErrorResponse.fromJson(Map<String, dynamic> json) {
    return ApiErrorResponse(
      code: json['code'] as String?,
      message: json['message'] as String?,
      details: json['details'],
    );
  }
}

class ApiResponse<T> {
  final bool success;
  final T? data;
  final ApiErrorResponse? error;

  const ApiResponse({
    required this.success,
    required this.data,
    required this.error,
  });

  factory ApiResponse.fromJson(
    Map<String, dynamic> json, {
    T Function(dynamic json)? dataParser,
  }) {
    return ApiResponse<T>(
      success: (json['success'] as bool?) ?? false,
      data: dataParser == null ? (json['data'] as T?) : dataParser(json['data']),
      error: json['error'] == null
          ? null
          : ApiErrorResponse.fromJson(json['error'] as Map<String, dynamic>),
    );
  }
}

