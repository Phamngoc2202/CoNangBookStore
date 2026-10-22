import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiClient {
  static const tokenKey = 'jwt_token';

  Future<Map<String, String>> headers({bool authenticated = true}) async {
    final result = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (authenticated) {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString(tokenKey);
      if (token != null && token.isNotEmpty) {
        result['Authorization'] = 'Bearer $token';
      }
    }
    return result;
  }

  Future<http.Response> get(Uri uri, {bool authenticated = true}) async =>
      http.get(uri, headers: await headers(authenticated: authenticated));

  Future<http.Response> post(Uri uri, {required String body}) async =>
      http.post(uri, headers: await headers(), body: body);

  Future<http.Response> put(Uri uri, {required String body}) async =>
      http.put(uri, headers: await headers(), body: body);

  Future<http.Response> delete(Uri uri) async =>
      http.delete(uri, headers: await headers());
}
