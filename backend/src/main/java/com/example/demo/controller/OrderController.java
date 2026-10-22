package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderStatusRequest;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderStatusHistory;
import com.example.demo.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // 1. Tạo đơn hàng
    @PostMapping
    public ResponseEntity<?> create(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request) {

       try{ Integer orderId = service.createOrder(
            authentication.getName(), request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Tạo đơn hàng thành công",
                "orderId", orderId
            )
        );
         } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                 Map.of("error", e.getMessage())
                );
          }
    }

    // 2. Xem đơn hàng của người đang đăng nhập
    @GetMapping("/my-orders")
    public List<Order> myOrders(Authentication authentication) {
        return service.getMyOrders(authentication.getName());
    }

    // 3. Xem thông tin đơn hàng
    @GetMapping("/{id}")
    public Order detail(
            Authentication authentication,
            @PathVariable Integer id) {

        return service.getOrderDetail(
            authentication.getName(), id
        );
    }

    // 4. Xem các dòng sách trong đơn
    @GetMapping("/{id}/items")
    public List<OrderItem> items(
            Authentication authentication,
            @PathVariable Integer id) {

        return service.getOrderItems(
            authentication.getName(), id
        );
    }

    // 5. Xem lịch sử trạng thái
    @GetMapping("/{id}/history")
    public List<OrderStatusHistory> history(
            Authentication authentication,
            @PathVariable Integer id) {

        return service.getMyOrderHistory(
            authentication.getName(), id
        );
    }

    // 6. Phê duyệt
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody(required = false)
            OrderStatusRequest request) {

        service.approve(
            authentication.getName(),
            id,
            request == null ? null : request.reason()
        );

        return ResponseEntity.ok(
            Map.of("message", "Phê duyệt đơn hàng thành công")
        );
    }

    // 7. Từ chối
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody(required = false)
            OrderStatusRequest request) {

        service.reject(
            authentication.getName(),
            id,
            request == null ? null : request.reason()
        );

        return ResponseEntity.ok(
            Map.of("message", "Đã từ chối đơn hàng")
        );
    }

    // 8. Hủy đơn
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody(required = false)
            OrderStatusRequest request) {

        service.cancel(
            authentication.getName(),
            id,
            request == null ? null : request.reason()
        );

        return ResponseEntity.ok(
            Map.of("message", "Đã hủy đơn hàng")
        );
    }

    // 9. Chuyển sang đang giao
    @PostMapping("/{id}/ship")
    public ResponseEntity<?> ship(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody(required = false)
            OrderStatusRequest request) {

        service.ship(
            authentication.getName(),
            id,
            request == null ? null : request.reason()
        );

        return ResponseEntity.ok(
            Map.of("message", "Đơn hàng đang được giao")
        );
    }

    // 10. Hoàn thành đơn
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody(required = false)
            OrderStatusRequest request) {

        service.complete(
            authentication.getName(),
            id,
            request == null ? null : request.reason()
        );

        return ResponseEntity.ok(
            Map.of("message", "Đơn hàng đã hoàn thành")
        );
    }
}
// package com.example.demo.controller;

// import com.example.demo.model.Order;
// import com.example.demo.model.OrderItem;
// import com.example.demo.service.OrderService;
// import com.example.demo.service.OrderItemService;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/orders")
// @CrossOrigin(origins = "*")
// public class OrderController {

//     private final OrderService orderService;
//     private final OrderItemService itemService;

//     public OrderController(
//             OrderService orderService,
//             OrderItemService itemService) {
//         this.orderService = orderService;
//         this.itemService = itemService;
//     }

//     @GetMapping
//     public List<Order> findAll() {
//         return orderService.findAll();
//     }

//     @GetMapping("/{id}")
//     public Order findById(@PathVariable Integer id) {
//         return orderService.findById(id);
//     }

//     @GetMapping("/{id}/items")
//     public List<OrderItem> findItems(@PathVariable Integer id) {
//         orderService.findById(id);
//         return itemService.findByOrderId(id);
//     }

//     @GetMapping("/user/{username}")
//     public List<Order> findByUsername(
//             @PathVariable String username) {
//         return orderService.findByUsername(username);
//     }

//     @PostMapping
//     public ResponseEntity<Order> create(
//             @RequestBody Order order) {
//         return ResponseEntity.status(HttpStatus.CREATED)
//             .body(orderService.create(order));
//     }

//     @PatchMapping("/{id}/status")
//     public Order updateStatus(
//             @PathVariable Integer id,
//             @RequestBody Map<String, String> body) {
//         return orderService.updateStatus(
//             id, body.get("status")
//         );
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(
//             @PathVariable Integer id) {
//         orderService.delete(id);
//         return ResponseEntity.noContent().build();
//     }
// }