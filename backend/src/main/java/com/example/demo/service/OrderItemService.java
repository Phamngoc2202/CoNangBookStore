// package com.example.demo.service;

// import com.example.demo.model.Order;
// import com.example.demo.model.OrderItem;
// import com.example.demo.repository.OrderItemRepository;
// import com.example.demo.repository.OrderRepository;
// import com.example.demo.repository.BookRepository;

// import org.springframework.stereotype.Service;

// import java.math.BigDecimal;
// import java.util.List;

// @Service
// public class OrderItemService {

//     private final OrderItemRepository itemRepository;
//     private final OrderRepository orderRepository;
//     private final BookRepository bookRepository;

//     public OrderItemService(
//             OrderItemRepository itemRepository,
//             OrderRepository orderRepository,
//             BookRepository bookRepository) {
//         this.itemRepository = itemRepository;
//         this.orderRepository = orderRepository;
//         this.bookRepository = bookRepository;
//     }

//     public List<OrderItem> findAll() {
//         return itemRepository.findAll();
//     }

//     public List<OrderItem> findByOrderId(Integer orderid) {
//         validateId(orderid, "orderid");
//         return itemRepository.findByOrderId(orderid);
//     }

//     public OrderItem findById(Integer orderid, Integer bookid) {
//         validateId(orderid, "orderid");
//         validateId(bookid, "bookid");

//         return itemRepository.findById(orderid, bookid)
//             .orElseThrow(() ->
//                 new IllegalArgumentException(
//                     "Không tìm thấy OrderItem."
//                 )
//             );
//     }

//     public OrderItem create(OrderItem item) {
//         validateItem(item);

//         // Kiểm tra đơn hàng và sách có tồn tại
//         orderRepository.findById(item.getOrderid())
//             .orElseThrow(() -> new IllegalArgumentException(
//                 "Không tìm thấy đơn hàng."
//             ));

//         bookRepository.findById(item.getBookid())
//             .orElseThrow(() -> new IllegalArgumentException(
//                 "Không tìm thấy sách."
//             ));

//         if (itemRepository.findById(
//                 item.getOrderid(), item.getBookid()).isPresent()) {
//             throw new IllegalArgumentException(
//                 "Sách đã tồn tại trong đơn hàng."
//             );
//         }

//         itemRepository.save(item);
//         return findById(item.getOrderid(), item.getBookid());
//     }

//     public OrderItem update(
//             Integer orderid, Integer bookid, OrderItem item) {

//         validateId(orderid, "orderid");
//         validateId(bookid, "bookid");
//         validateItem(item);

//         findById(orderid, bookid);

//         // Không cho phép thay đổi khóa của dòng chi tiết
//         item.setOrderid(orderid);
//         item.setBookid(bookid);

//         itemRepository.update(item);

//         return findById(orderid, bookid);
//     }

//     public void delete(Integer orderid, Integer bookid) {
//         findById(orderid, bookid);
//         itemRepository.delete(orderid, bookid);
//     }

//     private void validateItem(OrderItem item) {
//         if (item == null) {
//             throw new IllegalArgumentException(
//                 "OrderItem không được để trống."
//             );
//         }

//         validateId(item.getOrderid(), "orderid");
//         validateId(item.getBookid(), "bookid");

//         if (item.getQuantity() == null || item.getQuantity() <= 0) {
//             throw new IllegalArgumentException(
//                 "Quantity phải lớn hơn 0."
//             );
//         }

//         BigDecimal price = item.getPrice();

//         if (price == null
//                 || price.compareTo(BigDecimal.ZERO) < 0) {
//             throw new IllegalArgumentException(
//                 "Price phải lớn hơn hoặc bằng 0."
//             );
//         }
//     }

//     private void validateId(Integer id, String field) {
//         if (id == null || id <= 0) {
//             throw new IllegalArgumentException(
//                 field + " phải là số nguyên dương."
//             );
//         }
//     }
// }