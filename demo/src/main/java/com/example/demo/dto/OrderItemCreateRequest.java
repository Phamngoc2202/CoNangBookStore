
package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemCreateRequest(
    @NotNull(message = "Book ID bắt buộc")
    @Positive(message = "Book ID phải lớn hơn 0")
    Integer bookid,

    @NotNull(message = "Số lượng bắt buộc")
    @Positive(message = "Số lượng phải lớn hơn 0")
    Integer quantity
) {}
