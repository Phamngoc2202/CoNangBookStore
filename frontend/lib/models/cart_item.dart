import 'book.dart';

class CartItem {
  final Book book;
  final int quantity;

  const CartItem(this.book, this.quantity);

  double get subtotal => book.price * quantity;

  factory CartItem.fromJson(Map<String, dynamic> json) => CartItem(
        Book.fromJson(Map<String, dynamic>.from(json['book'] as Map)),
        (json['quantity'] as num).toInt(),
      );

  Map<String, dynamic> toJson() => {
        'book': {...book.toJson(), 'bookId': book.bookId},
        'quantity': quantity,
      };
}
