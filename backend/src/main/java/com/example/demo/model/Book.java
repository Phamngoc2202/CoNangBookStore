 package com.example.demo.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Book(
    Integer bookId,
    String isbn,
    String bookName,
    String author,
    String publisher,
    Integer categoryId,
    BigDecimal price,
    Integer stockQuantity,
    String imageURL,
    String description,
    String status,
    LocalDateTime createdAt
) {}
// import java.math.BigDecimal;

// public class Book {

//     private Integer bookid;
//     private String bookname;
//     private BigDecimal price;
//     private String author;
//     private String publisher;
//     private String summary;
//     private boolean status;

//     public Book() {
//     }

//     public Book(Integer bookid, String bookname, BigDecimal price,
//                 String author, String publisher, String summary,
//                 boolean status) {
//         this.bookid = bookid;
//         this.bookname = bookname;
//         this.price = price;
//         this.author = author;
//         this.publisher = publisher;
//         this.summary = summary;
//         this.status = status;
//     }

//     public Integer getBookid() {
//         return bookid;
//     }

//     public void setBookid(Integer bookid) {
//         this.bookid = bookid;
//     }

//     public String getBookname() {
//         return bookname;
//     }

//     public void setBookname(String bookname) {
//         this.bookname = bookname;
//     }

//     public BigDecimal getPrice() {
//         return price;
//     }

//     public void setPrice(BigDecimal price) {
//         this.price = price;
//     }

//     public String getAuthor() {
//         return author;
//     }

//     public void setAuthor(String author) {
//         this.author = author;
//     }

//     public String getPublisher() {
//         return publisher;
//     }

//     public void setPublisher(String publisher) {
//         this.publisher = publisher;
//     }

//     public String getSummary() {
//         return summary;
//     }

//     public void setSummary(String summary) {
//         this.summary = summary;
//     }

//     public boolean isStatus() {
//         return status;
//     }

//     public void setStatus(boolean status) {
//         this.status = status;
//     }
// }