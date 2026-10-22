// 
package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderItemRequest(
    @NotBlank(message = "Trạng thái đơn hàng bắt buộc")
    @Size(max = 30)
    String status,

    @NotBlank(message = "Địa chỉ giao hàng bắt buộc")
    @Size(max = 1000)
    String shippingAddress,

    @NotBlank(message = "Số điện thoại bắt buộc")
    @Size(max = 20)
    String phone,

    @Size(max = 2000)
    String note,

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    List<@Valid OrderItemCreateRequest> items
) {}
