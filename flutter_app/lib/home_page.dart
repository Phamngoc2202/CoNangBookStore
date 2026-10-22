import 'package:flutter/material.dart';

import 'models/book.dart';
import 'models/order.dart';
import 'services/book_service.dart';
import 'services/cart_store.dart';
import 'services/order_service.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.onLogout});

  final Future<void> Function() onLogout;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final CartStore _cart = CartStore();
  final OrderService _orderService = OrderService();
  final _formKey = GlobalKey<FormState>();
  final _address = TextEditingController();
  final _phone = TextEditingController();
  final _note = TextEditingController();
  late Future<void> _cartReady;
  late Future<List<Book>> _books;
  late Future<List<Order>> _orders;
  int _tab = 0;
  bool _placing = false;

  @override
  void initState() {
    super.initState();
    _cartReady = _cart.load();
    _books = BookService().getBooks();
    _orders = _orderService.getMyOrders();
  }

  @override
  void dispose() {
    _cart.dispose();
    _address.dispose();
    _phone.dispose();
    _note.dispose();
    super.dispose();
  }

  void _refresh() {
    setState(() {
      if (_tab == 0) _books = BookService().getBooks();
      if (_tab == 2) _orders = _orderService.getMyOrders();
    });
  }

  Future<void> _add(Book book) async {
    final added = await _cart.add(book);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(added
          ? 'Đã thêm ${book.bookName} vào giỏ hàng'
          : 'Không thể thêm: sách đã hết hàng hoặc vượt số lượng còn lại'),
    ));
  }

  Future<void> _placeOrder() async {
    if (_placing || !_formKey.currentState!.validate()) return;
    setState(() => _placing = true);
    try {
      final orderId = await _orderService.createOrder(
        items: _cart.items,
        shippingAddress: _address.text,
        phone: _phone.text,
        note: _note.text,
      );
      await _cart.clear();
      if (!mounted) return;
      _note.clear();
      setState(() {
        _books = BookService().getBooks();
        _orders = _orderService.getMyOrders();
        _tab = 2;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Đặt hàng thành công, mã đơn #$orderId')),
      );
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text(error.toString().replaceFirst('Exception: ', ''))),
        );
      }
    } finally {
      if (mounted) setState(() => _placing = false);
    }
  }

  String _money(double amount) => '${amount.toStringAsFixed(0)} ₫';

  @override
  Widget build(BuildContext context) => FutureBuilder<void>(
        future: _cartReady,
        builder: (context, ready) {
          if (ready.connectionState != ConnectionState.done) {
            return const Scaffold(
                body: Center(child: CircularProgressIndicator()));
          }
          if (ready.hasError) {
            return Scaffold(
              body: Center(
                  child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('Không mở được giỏ hàng: ${ready.error}'),
                  TextButton(
                      onPressed: widget.onLogout,
                      child: const Text('Đăng nhập lại')),
                ],
              )),
            );
          }
          return AnimatedBuilder(
            animation: _cart,
            builder: (context, _) => Scaffold(
              appBar: AppBar(
                title: Text(
                    ['Danh sách sách', 'Giỏ hàng', 'Đơn hàng đã đặt'][_tab]),
                actions: [
                  if (_tab != 1)
                    IconButton(
                      tooltip: 'Tải lại',
                      onPressed: _refresh,
                      icon: const Icon(Icons.refresh),
                    ),
                  IconButton(
                    tooltip: 'Đăng xuất',
                    onPressed: widget.onLogout,
                    icon: const Icon(Icons.logout),
                  ),
                ],
              ),
              body: switch (_tab) {
                0 => _bookList(),
                1 => _cartView(),
                _ => _orderList(),
              },
              bottomNavigationBar: NavigationBar(
                selectedIndex: _tab,
                onDestinationSelected: (index) => setState(() {
                  _tab = index;
                  if (index == 2) _orders = _orderService.getMyOrders();
                }),
                destinations: [
                  const NavigationDestination(
                    icon: Icon(Icons.menu_book_outlined),
                    label: 'Sách',
                  ),
                  NavigationDestination(
                    icon: Badge(
                      isLabelVisible: _cart.itemCount > 0,
                      label: Text('${_cart.itemCount}'),
                      child: const Icon(Icons.shopping_cart_outlined),
                    ),
                    label: 'Giỏ hàng',
                  ),
                  const NavigationDestination(
                    icon: Icon(Icons.receipt_long_outlined),
                    label: 'Đơn hàng',
                  ),
                ],
              ),
            ),
          );
        },
      );

  Widget _bookList() => FutureBuilder<List<Book>>(
        future: _books,
        builder: (context, result) {
          if (result.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (result.hasError) {
            return Center(child: Text('Không tải được sách: ${result.error}'));
          }
          final books = result.data ?? [];
          if (books.isEmpty) {
            return const Center(
                child: Text('Chưa có sách trong cơ sở dữ liệu.'));
          }
          return ListView.builder(
            itemCount: books.length,
            itemBuilder: (context, index) {
              final book = books[index];
              final availability = !book.isActive
                  ? 'Ngừng kinh doanh'
                  : book.stockQuantity <= 0
                      ? 'Hết hàng'
                      : 'Còn ${book.stockQuantity} cuốn';
              return ListTile(
                title: Text(book.bookName),
                subtitle: Text(
                    '${book.author} · $availability · ${_money(book.price)}'),
                trailing: IconButton(
                  tooltip: book.canOrder ? 'Thêm vào giỏ hàng' : availability,
                  onPressed: book.canOrder ? () => _add(book) : null,
                  icon: const Icon(Icons.add_shopping_cart),
                ),
              );
            },
          );
        },
      );

  Widget _cartView() {
    final items = _cart.items;
    if (items.isEmpty) {
      return const Center(child: Text('Giỏ hàng đang trống.'));
    }
    return Center(
        child: ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 760),
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          for (final item in items)
            Card(
                child: ListTile(
              title: Text(item.book.bookName),
              subtitle: Text(
                  '${_money(item.book.price)} × ${item.quantity} = ${_money(item.subtotal)}'),
              trailing: Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  IconButton(
                    tooltip: 'Giảm số lượng',
                    onPressed: _placing
                        ? null
                        : () => _cart.setQuantity(
                            item.book.bookId!, item.quantity - 1),
                    icon: const Icon(Icons.remove_circle_outline),
                  ),
                  Text('${item.quantity}'),
                  IconButton(
                    tooltip: 'Tăng số lượng',
                    onPressed:
                        _placing || item.quantity >= item.book.stockQuantity
                            ? null
                            : () => _cart.setQuantity(
                                item.book.bookId!, item.quantity + 1),
                    icon: const Icon(Icons.add_circle_outline),
                  ),
                  IconButton(
                    tooltip: 'Xóa khỏi giỏ',
                    onPressed: _placing
                        ? null
                        : () => _cart.setQuantity(item.book.bookId!, 0),
                    icon: const Icon(Icons.delete_outline),
                  ),
                ],
              ),
            )),
          const SizedBox(height: 12),
          Text('Tạm tính: ${_money(_cart.total)}',
              style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 20),
          Form(
            key: _formKey,
            child: Column(children: [
              TextFormField(
                controller: _address,
                decoration:
                    const InputDecoration(labelText: 'Địa chỉ giao hàng'),
                maxLength: 1000,
                validator: (value) => value == null || value.trim().isEmpty
                    ? 'Hãy nhập địa chỉ giao hàng'
                    : null,
              ),
              TextFormField(
                controller: _phone,
                decoration: const InputDecoration(labelText: 'Số điện thoại'),
                keyboardType: TextInputType.phone,
                maxLength: 20,
                validator: (value) => value == null || value.trim().isEmpty
                    ? 'Hãy nhập số điện thoại'
                    : null,
              ),
              TextFormField(
                controller: _note,
                decoration: const InputDecoration(
                    labelText: 'Ghi chú (không bắt buộc)'),
                maxLength: 2000,
              ),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _placing ? null : _placeOrder,
                icon: const Icon(Icons.shopping_bag_outlined),
                label: Text(_placing ? 'Đang đặt hàng...' : 'Đặt hàng'),
              ),
            ]),
          ),
        ],
      ),
    ));
  }

  Widget _orderList() => FutureBuilder<List<Order>>(
        future: _orders,
        builder: (context, result) {
          if (result.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (result.hasError) {
            return Center(
                child: Text('Không tải được đơn hàng: ${result.error}'));
          }
          final orders = result.data ?? [];
          if (orders.isEmpty) {
            return const Center(child: Text('Bạn chưa đặt đơn hàng nào.'));
          }
          return ListView.builder(
            itemCount: orders.length,
            itemBuilder: (context, index) {
              final order = orders[index];
              final date = order.orderDate;
              final dateText = date == null
                  ? ''
                  : '${date.day}/${date.month}/${date.year} ${date.hour}:${date.minute.toString().padLeft(2, '0')}';
              return ListTile(
                title: Text('Đơn #${order.orderId} · ${order.status}'),
                subtitle: Text('$dateText\n${order.shippingAddress}'),
                isThreeLine: true,
                trailing: Text(_money(order.totalAmount)),
                onTap: () => _showOrderItems(order),
              );
            },
          );
        },
      );

  Future<void> _showOrderItems(Order order) async {
    await showModalBottomSheet<void>(
      context: context,
      builder: (context) => SafeArea(
          child: FutureBuilder<List<OrderLine>>(
        future: _orderService.getItems(order.orderId),
        builder: (context, result) {
          if (result.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (result.hasError) {
            return Center(
                child: Text('Không tải được chi tiết: ${result.error}'));
          }
          return ListView(
            children: [
              ListTile(title: Text('Chi tiết đơn #${order.orderId}')),
              for (final line in result.data ?? <OrderLine>[])
                ListTile(
                  title: Text('Sách ID ${line.bookId}'),
                  subtitle: Text('Số lượng: ${line.quantity}'),
                  trailing: Text(_money(line.subtotal)),
                ),
            ],
          );
        },
      )),
    );
  }
}
