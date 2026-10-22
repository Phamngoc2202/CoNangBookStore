// package com.example.demo.controller;

// import com.example.demo.model.OrderItem;
// import com.example.demo.service.OrderItemService;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/order-items")
// @CrossOrigin(origins = "*")
// public class OrderItemController {

//     private final OrderItemService service;

//     public OrderItemController(OrderItemService service) {
//         this.service = service;
//     }

//     @GetMapping
//     public List<OrderItem> findAll() {
//         return service.findAll();
//     }

//     @GetMapping("/{orderid}/{bookid}")
//     public OrderItem findById(
//             @PathVariable Integer orderid,
//             @PathVariable Integer bookid) {
//         return service.findById(orderid, bookid);
//     }

//     @GetMapping("/order/{orderid}")
//     public List<OrderItem> findByOrderId(
//             @PathVariable Integer orderid) {
//         return service.findByOrderId(orderid);
//     }

//     @PostMapping
//     public ResponseEntity<OrderItem> create(
//             @RequestBody OrderItem item) {
//         return ResponseEntity.status(HttpStatus.CREATED)
//             .body(service.create(item));
//     }

//     @PutMapping("/{orderid}/{bookid}")
//     public OrderItem update(
//             @PathVariable Integer orderid,
//             @PathVariable Integer bookid,
//             @RequestBody OrderItem item) {
//         return service.update(orderid, bookid, item);
//     }

//     @DeleteMapping("/{orderid}/{bookid}")
//     public ResponseEntity<Void> delete(
//             @PathVariable Integer orderid,
//             @PathVariable Integer bookid) {
//         service.delete(orderid, bookid);
//         return ResponseEntity.noContent().build();
//     }
// }