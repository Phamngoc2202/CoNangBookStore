import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:bookstore_flutter/models/book.dart';
import 'package:bookstore_flutter/services/api_client.dart';
import 'package:bookstore_flutter/services/cart_store.dart';

String tokenFor(String userId) {
  final payload = base64Url.encode(utf8.encode(jsonEncode({
    'sub': userId,
    'exp': DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch ~/ 1000,
  })));
  return 'header.$payload.signature';
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('cart persists across reloads and stays separate for each user', () async {
    SharedPreferences.setMockInitialValues({
      ApiClient.tokenKey: tokenFor('user-1'),
    });

    const book = Book(
      bookId: 5,
      isbn: 'test-5',
      bookName: 'Test book',
      author: 'Author',
      categoryId: 1,
      price: 100,
      stockQuantity: 3,
    );

    final first = CartStore();
    await first.load();
    expect(await first.add(book), isTrue);
    expect(await first.add(book), isTrue);
    expect(first.itemCount, 2);

    final restored = CartStore();
    await restored.load();
    expect(restored.itemCount, 2);
    expect(restored.total, 200);

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(ApiClient.tokenKey, tokenFor('user-2'));
    final anotherUser = CartStore();
    await anotherUser.load();
    expect(anotherUser.items, isEmpty);

    first.dispose();
    restored.dispose();
    anotherUser.dispose();
  });
}
