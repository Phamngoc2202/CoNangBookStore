import 'dart:convert';

import '../config/api_config.dart';
import '../models/cart_item.dart';
import '../models/order.dart';
import 'api_client.dart';

class OrderService {
  final ApiClient _client = ApiClient();

  Future<int> createOrder({
    required List<CartItem> items,
    required String shippingAddress,
    required String phone,
    String? note,
  }) async {
    if (items.isEmpty) throw Exception('Giỏ hàng đang trống.');
    final response = await _client.post(
      Uri.parse(ApiConfig.orders),
      body: jsonEncode({
        'status': 'PENDING',
        'shippingAddress': shippingAddress.trim(),
        'phone': phone.trim(),
        'note': note?.trim(),
        'items': [
          for (final item in items)
            {'bookid': item.book.bookId, 'quantity': item.quantity},
        ],
      }),
    );
    if (response.statusCode != 201) {
      throw Exception(_error(response.body, response.statusCode));
    }
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    return (data['orderId'] as num).toInt();
  }

  Future<List<Order>> getMyOrders() async {
    final response =
        await _client.get(Uri.parse('${ApiConfig.orders}/my-orders'));
    if (response.statusCode != 200) {
      throw Exception(_error(response.body, response.statusCode));
    }
    final data = jsonDecode(response.body) as List;
    return data
        .map((value) => Order.fromJson(Map<String, dynamic>.from(value as Map)))
        .toList();
  }

  Future<List<OrderLine>> getItems(int orderId) async {
    final response =
        await _client.get(Uri.parse('${ApiConfig.orders}/$orderId/items'));
    if (response.statusCode != 200) {
      throw Exception(_error(response.body, response.statusCode));
    }
    final data = jsonDecode(response.body) as List;
    return data
        .map((value) =>
            OrderLine.fromJson(Map<String, dynamic>.from(value as Map)))
        .toList();
  }

  String _error(String body, int status) {
    try {
      final data = jsonDecode(body);
      if (data is Map) {
        return (data['error'] ?? data['message'])?.toString() ?? 'HTTP $status';
      }
    } catch (_) {}
    return 'HTTP $status: $body';
  }
}
