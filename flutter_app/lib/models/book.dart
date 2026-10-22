class Book {
  final int? bookId;
  final String isbn;
  final String bookName;
  final String author;
  final String? publisher;
  final int categoryId;
  final double price;
  final int stockQuantity;
  final String? imageURL;
  final String? description;
  final String status;

  const Book({
    this.bookId,
    required this.isbn,
    required this.bookName,
    required this.author,
    this.publisher,
    required this.categoryId,
    required this.price,
    required this.stockQuantity,
    this.imageURL,
    this.description,
    this.status = 'ACTIVE',
  });

  bool get isActive => status.toUpperCase() == 'ACTIVE';
  bool get canOrder => isActive && stockQuantity > 0;

  factory Book.fromJson(Map<String, dynamic> json) => Book(
        bookId: (json['bookId'] as num?)?.toInt(),
        isbn: json['isbn']?.toString() ?? '',
        bookName: json['bookName']?.toString() ?? '',
        author: json['author']?.toString() ?? '',
        publisher: json['publisher']?.toString(),
        categoryId: (json['categoryId'] as num?)?.toInt() ?? 0,
        price: (json['price'] as num?)?.toDouble() ?? 0,
        stockQuantity: (json['stockQuantity'] as num?)?.toInt() ?? 0,
        imageURL: json['imageURL']?.toString(),
        description: json['description']?.toString(),
        status: json['status']?.toString() ?? 'ACTIVE',
      );

  Map<String, dynamic> toJson() => {
        'isbn': isbn,
        'bookName': bookName,
        'author': author,
        'publisher': publisher,
        'categoryId': categoryId,
        'price': price,
        'stockQuantity': stockQuantity,
        'imageURL': imageURL,
        'description': description,
        'status': status,
      };
}
