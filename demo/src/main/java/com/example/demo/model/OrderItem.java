// package com.example.demo.model;
package com.example.demo.model;

import java.math.BigDecimal;

public record OrderItem(
    Integer orderItemId,
    Integer orderId,
    Integer bookId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
// import java.math.BigDecimal;

// public class OrderItem {

//     private Integer orderid;
//     private Integer bookid;
//     private Integer quantity;
//     private BigDecimal price;

//     public OrderItem() {
//     }

//     public OrderItem(Integer orderid, Integer bookid,
//                      Integer quantity, BigDecimal price) {
//         this.orderid = orderid;
//         this.bookid = bookid;
//         this.quantity = quantity;
//         this.price = price;
//     }

//     public Integer getOrderid() {
//         return orderid;
//     }

//     public void setOrderid(Integer orderid) {
//         this.orderid = orderid;
//     }

//     public Integer getBookid() {
//         return bookid;
//     }

//     public void setBookid(Integer bookid) {
//         this.bookid = bookid;
//     }

//     public Integer getQuantity() {
//         return quantity;
//     }

//     public void setQuantity(Integer quantity) {
//         this.quantity = quantity;
//     }

//     public BigDecimal getPrice() {
//         return price;
//     }

//     public void setPrice(BigDecimal price) {
//         this.price = price;
//     }
// }