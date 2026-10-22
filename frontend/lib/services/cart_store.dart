import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/book.dart';
import '../models/cart_item.dart';
import 'auth_service.dart';

class CartStore extends ChangeNotifier {
  final Map<int, CartItem> _items = {};
  String? _storageKey;

  List<CartItem> get items => _items.values.toList()
    ..sort((a, b) => a.book.bookName.compareTo(b.book.bookName));
  int get itemCount =>
      _items.values.fold(0, (sum, item) => sum + item.quantity);
  double get total => _items.values.fold(0, (sum, item) => sum + item.subtotal);

  Future<void> load() async {
    final userId = await AuthService().getUserId();
    if (userId == null) throw StateError('Phiên đăng nhập đã hết hạn.');
    _storageKey = 'cart_$userId';
    _items.clear();
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString(_storageKey!);
    if (saved != null) {
      try {
        for (final value in jsonDecode(saved) as List) {
          final item =
              CartItem.fromJson(Map<String, dynamic>.from(value as Map));
          final id = item.book.bookId;
          if (id != null && item.quantity > 0) _items[id] = item;
        }
      } catch (_) {
        _items.clear();
      }
    }
    notifyListeners();
  }

  Future<bool> add(Book book) async {
    final id = book.bookId;
    if (id == null || !book.canOrder) {
      return false;
    }
    final next = (_items[id]?.quantity ?? 0) + 1;
    if (next > book.stockQuantity) return false;
    _items[id] = CartItem(book, next);
    notifyListeners();
    await _save();
    return true;
  }

  Future<void> setQuantity(int bookId, int quantity) async {
    final current = _items[bookId];
    if (current == null) return;
    if (quantity <= 0) {
      _items.remove(bookId);
    } else if (quantity <= current.book.stockQuantity) {
      _items[bookId] = CartItem(current.book, quantity);
    } else {
      return;
    }
    notifyListeners();
    await _save();
  }

  Future<void> clear() async {
    _items.clear();
    notifyListeners();
    await _save();
  }

  Future<void> _save() async {
    if (_storageKey == null) return;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(
      _storageKey!,
      jsonEncode(_items.values.map((item) => item.toJson()).toList()),
    );
  }
}
