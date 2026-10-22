class Order {
  final int orderId;
  final DateTime? orderDate;
  final double totalAmount;
  final String shippingAddress;
  final String status;

  const Order({
    required this.orderId,
    required this.orderDate,
    required this.totalAmount,
    required this.shippingAddress,
    required this.status,
  });

  factory Order.fromJson(Map<String, dynamic> json) => Order(
        orderId: (json['orderId'] as num).toInt(),
        orderDate: DateTime.tryParse(json['orderDate']?.toString() ?? ''),
        totalAmount: (json['totalAmount'] as num).toDouble(),
        shippingAddress: json['shippingAddress']?.toString() ?? '',
        status: json['status']?.toString() ?? '',
      );
}

class OrderLine {
  final int bookId;
  final int quantity;
  final double subtotal;

  const OrderLine({
    required this.bookId,
    required this.quantity,
    required this.subtotal,
  });

  factory OrderLine.fromJson(Map<String, dynamic> json) => OrderLine(
        bookId: (json['bookId'] as num).toInt(),
        quantity: (json['quantity'] as num).toInt(),
        subtotal: (json['subtotal'] as num).toDouble(),
      );
}
