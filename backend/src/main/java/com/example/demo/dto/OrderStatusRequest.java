package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public record OrderStatusRequest(
    @Size(max = 2000)
    String reason
) {}