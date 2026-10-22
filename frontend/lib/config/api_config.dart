import 'package:flutter/foundation.dart';

class ApiConfig {
  // For a physical device: --dart-define=API_BASE_URL=http://IP:8080/api
  static const String _override = String.fromEnvironment('API_BASE_URL');

  static String get baseUrl {
    if (_override.isNotEmpty) return _override;
    return !kIsWeb && defaultTargetPlatform == TargetPlatform.android
        ? 'http://10.0.2.2:8080/api'
        : 'http://localhost:8080/api';
  }

  static String get register => '$baseUrl/auth/register';
  static String get login => '$baseUrl/auth/login';
  static String get books => '$baseUrl/books';
  static String get orders => '$baseUrl/orders';
}
