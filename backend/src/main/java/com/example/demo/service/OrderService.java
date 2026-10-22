package com.example.demo.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderItemCreateRequest;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderStatusHistory;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderRepository.BookInfo;

@Service
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    
@Transactional
public Integer createOrder(
        String username,
        OrderRequest request) {

    Integer userId = repo.findUserIdByUsername(username);

    if (userId == null) {
        throw new IllegalArgumentException(
            "Không tìm thấy người dùng: " + username
        );
    }

    Set<Integer> bookIds = new HashSet<>();
    Map<Integer, BookInfo> books = new HashMap<>();
    BigDecimal total = BigDecimal.ZERO;

    // 1. Kiểm tra dữ liệu và tính tổng tiền từ database.
    for (OrderItemCreateRequest item : request.items()) {

        if (!bookIds.add(item.bookid())) {
            throw new IllegalArgumentException(
                "Sách ID " + item.bookid()
                + " bị lặp trong đơn hàng"
            );
        }

        BookInfo book = repo.findBook(item.bookid())
            .orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy sách ID " + item.bookid()
            ));

        if (!"ACTIVE".equalsIgnoreCase(book.status())) {
            throw new IllegalArgumentException(
                "Sách không còn kinh doanh: " + book.bookName()
            );
        }

        if (book.price() == null
                || book.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Giá sách không hợp lệ: " + book.bookName()
            );
        }

        if (book.stockQuantity() < item.quantity()) {
            throw new IllegalArgumentException(
                "Không đủ tồn kho cho sách: " + book.bookName()
            );
        }

        total = total.add(
            book.price().multiply(
                BigDecimal.valueOf(item.quantity())
            )
        );
        books.put(item.bookid(), book);
    }

    // Reserve stock in the same transaction as the order. The condition in
    // decreaseStock protects against another order consuming the last copy.
    for (OrderItemCreateRequest item : request.items().stream()
            .sorted((a, b) -> Integer.compare(a.bookid(), b.bookid()))
            .toList()) {
        if (repo.decreaseStock(item.bookid(), item.quantity()) != 1) {
            throw new IllegalArgumentException(
                "Không đủ tồn kho hoặc sách đã ngừng kinh doanh: "
                    + books.get(item.bookid()).bookName()
            );
        }
    }

    // 2. Tạo đơn hàng; repository trả về ID mới.
    Integer orderId = repo.insertOrder(
        userId,
        total,
        request.shippingAddress(),
        request.phone(),
        request.note()
    );

    // 3. Lưu chi tiết đơn với ID và giá lấy từ backend.
    for (OrderItemCreateRequest item : request.items()) {

        BookInfo book = books.get(item.bookid());

        repo.insertOrderItem(
            orderId,
            book.bookId(),
            item.quantity(),
            book.price()
        );
    }

    // 4. Ghi lịch sử trạng thái.
    repo.insertHistory(
        orderId,
        null,
        "PENDING",
        userId,
        OrderRepository.STOCK_RESERVED_REASON
    );

    return orderId;
}

    public List<Order> getMyOrders(String username) {
        Integer userId = repo.findUserIdByUsername(username);
        return repo.findOrdersByUser(userId);
    }

    public Order getOrderDetail(
            String username, Integer orderId) {

        Integer userId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);

        if (!order.userId().equals(userId)) {
            throw new SecurityException(
                "Bạn không có quyền xem đơn hàng này"
            );
        }

        return order;
    }

    public List<OrderItem> getOrderItems(
            String username, Integer orderId) {

        getOrderDetail(username, orderId);
        return repo.findItems(orderId);
    }

    public List<OrderStatusHistory> getMyOrderHistory(
            String username, Integer orderId) {

        getOrderDetail(username, orderId);
        return repo.findHistory(orderId);
    }

    @Transactional
    public void approve(
            String username, Integer orderId, String reason) {

        Integer approverId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);

        requireStatus(order, "PENDING");

        List<OrderItem> items = repo.findItems(orderId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                "Đơn hàng không có sản phẩm"
            );
        }

        // Pending orders created by the old version did not reserve stock.
        if (!repo.wasStockReservedAtCreation(orderId)) {
            for (OrderItem item : items) {
                if (repo.decreaseStock(item.bookId(), item.quantity()) != 1) {
                    throw new IllegalArgumentException(
                        "Không đủ tồn kho cho sách ID " + item.bookId()
                    );
                }
            }
        }

        changeStatus(orderId, "PENDING", "APPROVED", approverId, true);

        repo.insertHistory(
            orderId, order.status(), "APPROVED",
            approverId, reason
        );
    }

    @Transactional
    public void reject(
            String username, Integer orderId, String reason) {

        Integer actorId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);

        requireStatus(order, "PENDING");

        changeStatus(orderId, "PENDING", "REJECTED", null, false);
        if (repo.wasStockReservedAtCreation(orderId)) {
            restoreStock(orderId);
        }
        repo.insertHistory(
            orderId, order.status(), "REJECTED", actorId, reason
        );
    }

    @Transactional
    public void cancel(
            String username, Integer orderId, String reason) {

        Integer actorId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);

        if (!order.userId().equals(actorId)) {
            throw new SecurityException(
                "Bạn không có quyền hủy đơn hàng này"
            );
        }

        if (!"PENDING".equals(order.status())
                && !"APPROVED".equals(order.status())) {
            throw new IllegalArgumentException(
                "Chỉ được hủy đơn đang PENDING hoặc APPROVED"
            );
        }

        changeStatus(orderId, order.status(), "CANCELLED", null, false);
        if ("APPROVED".equals(order.status())
                || repo.wasStockReservedAtCreation(orderId)) {
            restoreStock(orderId);
        }
        repo.insertHistory(
            orderId, order.status(), "CANCELLED", actorId, reason
        );
    }

    @Transactional
    public void ship(
            String username, Integer orderId, String reason) {

        Integer actorId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);
        requireStatus(order, "APPROVED");

        changeStatus(orderId, "APPROVED", "SHIPPING", null, false);
        repo.insertHistory(
            orderId, order.status(), "SHIPPING", actorId, reason
        );
    }

    @Transactional
    public void complete(
            String username, Integer orderId, String reason) {

        Integer actorId = repo.findUserIdByUsername(username);
        Order order = getRequiredOrder(orderId);
        requireStatus(order, "SHIPPING");

        changeStatus(orderId, "SHIPPING", "COMPLETED", null, false);
        repo.insertHistory(
            orderId, order.status(), "COMPLETED", actorId, reason
        );
    }

    private Order getRequiredOrder(Integer orderId) {
        return repo.findOrder(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy đơn hàng ID " + orderId
            ));
    }

    private void changeStatus(Integer orderId, String expected, String next,
            Integer approvedBy, boolean setApproval) {
        if (repo.updateStatus(orderId, expected, next, approvedBy, setApproval) != 1) {
            throw new IllegalArgumentException(
                "Trạng thái đơn hàng đã thay đổi, vui lòng tải lại"
            );
        }
    }

    private void restoreStock(Integer orderId) {
        for (OrderItem item : repo.findItems(orderId)) {
            if (repo.increaseStock(item.bookId(), item.quantity()) != 1) {
                throw new IllegalStateException(
                    "Không thể hoàn tồn kho cho sách ID " + item.bookId()
                );
            }
        }
    }

    private void requireStatus(Order order, String expected) {
        if (!expected.equals(order.status())) {
            throw new IllegalArgumentException(
                "Trạng thái hiện tại là " + order.status()
                + ", yêu cầu trạng thái " + expected
            );
        }
    }
}
// package com.example.demo.service;

// import com.example.demo.model.Order;
// import com.example.demo.repository.OrderRepository;
// import com.example.demo.repository.OrderItemRepository;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.List;

// @Service
// public class OrderService {

//     private final OrderRepository orderRepository;
//     private final OrderItemRepository itemRepository;

//     public OrderService(
//             OrderRepository orderRepository,
//             OrderItemRepository itemRepository) {
//         this.orderRepository = orderRepository;
//         this.itemRepository = itemRepository;
//     }

//     public List<Order> findAll() {
//         return orderRepository.findAll();
//     }

//     public Order findById(Integer id) {
//         validateId(id);

//         return orderRepository.findById(id)
//             .orElseThrow(() ->
//                 new IllegalArgumentException(
//                     "Không tìm thấy đơn hàng ID = " + id
//                 )
//             );
//     }

//     public List<Order> findByUsername(String username) {
//         if (username == null || username.isBlank()) {
//             throw new IllegalArgumentException(
//                 "Username không được để trống."
//             );
//         }

//         return orderRepository.findByUsername(username.trim());
//     }

//     public Order create(Order order) {
//         if (order == null
//                 || order.getUsername() == null
//                 || order.getUsername().isBlank()) {
//             throw new IllegalArgumentException(
//                 "Username không được để trống."
//             );
//         }

//         if (order.getStatus() == null
//                 || order.getStatus().isBlank()) {
//             order.setStatus("PENDING");
//         }

//         order.setId(null);
//         order.setUsername(order.getUsername().trim());

//         if (order.getDate() == null) {
//             order.setDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
//         }

//         int id = orderRepository.save(order);
//         return findById(id);
//     }

//     public Order updateStatus(Integer id, String status) {
//         validateId(id);

//         if (status == null || status.isBlank()) {
//             throw new IllegalArgumentException(
//                 "Status không được để trống."
//             );
//         }

//         findById(id);

//         orderRepository.updateStatus(id, status.trim());

//         return findById(id);
//     }

//     @Transactional
//     public void delete(Integer id) {
//         validateId(id);
//         findById(id);

//         // Xóa chi tiết trước để tránh vi phạm khóa ngoại
//         itemRepository.deleteByOrderId(id);
//         orderRepository.deleteById(id);
//     }

//     private void validateId(Integer id) {
//         if (id == null || id <= 0) {
//             throw new IllegalArgumentException(
//                 "ID phải là số nguyên dương."
//             );
//         }
//     }
// }
