package com.example.demo.dto;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BookRequest(
    @NotBlank(message = "Tên sách bắt buộc")
    @Size(max = 255)
    String bookname,

    @NotNull(message = "Giá sách bắt buộc")
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    @Digits(integer = 15, fraction = 4)
    BigDecimal price,

    @Size(max = 150) String author,
    @Size(max = 150) String publisher,
    String summary,
    Boolean status
) {}
