package com.example.demo.model;



public record Category(
    Integer categoryId,
    String categoryName,
    String description,
    String status
) {}