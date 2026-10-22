import 'dart:convert';

import '../config/api_config.dart';
import '../models/book.dart';
import 'api_client.dart';

class BookService {
  final ApiClient _client = ApiClient();

  Future<List<Book>> getBooks() async {
    final response = await _client.get(Uri.parse(ApiConfig.books));
    if (response.statusCode != 200) {
      throw Exception('Không tải được sách (HTTP ${response.statusCode}).');
    }
    final data = jsonDecode(response.body);
    if (data is! List) {
      throw Exception('API sách không trả về danh sách.');
    }
    return data
        .map((item) => Book.fromJson(
              Map<String, dynamic>.from(item as Map),
            ))
        .toList();
  }

  Future<void> createBook(Book book) async {
    final response = await _client.post(
      Uri.parse(ApiConfig.books),
      body: jsonEncode(book.toJson()),
    );
    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Không tạo được sách (HTTP ${response.statusCode}).');
    }
  }

  Future<void> updateBook(Book book) async {
    if (book.bookId == null) throw Exception('Không có BookID để cập nhật.');
    final response = await _client.put(
      Uri.parse('${ApiConfig.books}/${book.bookId}'),
      body: jsonEncode(book.toJson()),
    );
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception(
          'Không cập nhật được sách (HTTP ${response.statusCode}).');
    }
  }

  Future<void> deleteBook(int bookId) async {
    final response = await _client.delete(
      Uri.parse('${ApiConfig.books}/$bookId'),
    );
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Không xóa được sách (HTTP ${response.statusCode}).');
    }
  }
}
