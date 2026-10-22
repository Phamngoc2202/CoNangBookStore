package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record Order(
    Integer orderId,
    Integer userId,
    LocalDateTime orderDate,
    BigDecimal totalAmount,
    String shippingAddress,
    String phone,
    String status,
    Integer approvedBy,
    LocalDateTime approvedAt,
    String note
) {}
// public class Order {

//     private Integer id;
//     private String username;
//     private LocalDateTime date;
//     private String status;

//     public Order() {
//     }

//     public Order(Integer id, String username,
//                  LocalDateTime date, String status) {
//         this.id = id;
//         this.username = username;
//         this.date = date;
//         this.status = status;
//     }

//     public Integer getId() {
//         return id;
//     }

//     public void setId(Integer id) {
//         this.id = id;
//     }

//     public String getUsername() {
//         return username;
//     }

//     public void setUsername(String username) {
//         this.username = username;
//     }

//     public LocalDateTime getDate() {
//         return date;
//     }

//     public void setDate(LocalDateTime date) {
//         this.date = date;
//     }

//     public String getStatus() {
//         return status;
//     }

//     public void setStatus(String status) {
//         this.status = status;
//     }
// }