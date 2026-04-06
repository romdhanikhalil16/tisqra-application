import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/config/app_env.dart';
import 'package:mobile/core/network/api_client.dart';

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient.create(AppEnv.apiBaseUrl),
);

