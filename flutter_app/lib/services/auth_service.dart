import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

import '../config/api_config.dart';
import 'api_client.dart';

class AuthService {
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(ApiClient.tokenKey);
  }

  Map<String, dynamic>? _claims(String token) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) return null;
      final decoded = jsonDecode(utf8.decode(base64Url.decode(
        base64Url.normalize(parts[1]),
      )));
      return decoded is Map<String, dynamic> ? decoded : null;
    } catch (_) {
      return null;
    }
  }

  bool _validToken(String token) {
    final claims = _claims(token);
    if (claims == null) return false;
    final exp = claims['exp'];
    if (exp is! num) return false;
    return DateTime.fromMillisecondsSinceEpoch((exp * 1000).toInt())
        .isAfter(DateTime.now());
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && _validToken(token);
  }

  Future<String?> getRole() async {
    final token = await getToken();
    if (token == null || !_validToken(token)) return null;
    return _claims(token)?['role']?.toString().toUpperCase();
  }

  Future<String?> getUserId() async {
    final token = await getToken();
    if (token == null || !_validToken(token)) return null;
    return _claims(token)?['sub']?.toString();
  }

  Future<bool> canManageBooks() async {
    final role = await getRole();
    return role == 'ADMIN' ||
        role == 'STAFF' ||
        role == 'ROLE_ADMIN' ||
        role == 'ROLE_STAFF';
  }

  Future<void> login(
      {required String username, required String password}) async {
    await logout();
    final response = await http.post(
      Uri.parse(ApiConfig.login),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': username.trim(), 'password': password}),
    );
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception(_errorMessage(response));
    }
    final dynamic data;
    try {
      data = jsonDecode(response.body);
    } catch (_) {
      throw Exception('Phản hồi đăng nhập không phải JSON hợp lệ.');
    }
    if (data is! Map<String, dynamic>) {
      throw Exception('Phản hồi đăng nhập không đúng định dạng.');
    }
    final token = data['accessToken'];
    if (token is! String || !_validToken(token)) {
      throw Exception('API không trả về JWT hợp lệ hoặc token đã hết hạn.');
    }
    final prefs = await SharedPreferences.getInstance();
    if (!await prefs.setString(ApiClient.tokenKey, token)) {
      throw Exception('Không lưu được token trên thiết bị.');
    }
  }

  Future<void> signup({
    required String username,
    required String email,
    required String password,
    required String fullName,
  }) async {
    final response = await http.post(
      Uri.parse(ApiConfig.register),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'email': email,
        'password': password,
        'fullName': fullName,
      }),
    );
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception(_errorMessage(response));
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(ApiClient.tokenKey);
  }

  String _errorMessage(http.Response response) {
    try {
      final data = jsonDecode(response.body);
      if (data is Map<String, dynamic>) {
        return (data['message'] ?? data['error'])?.toString() ??
            'HTTP ${response.statusCode}';
      }
    } catch (_) {}
    return 'HTTP ${response.statusCode}: ${response.body}';
  }
}
