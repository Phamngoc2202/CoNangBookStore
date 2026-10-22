package com.example.demo.model;

import java.time.LocalDateTime;

public record OrderStatusHistory(
    Integer historyId,
    Integer orderId,
    String oldStatus,
    String newStatus,
    Integer changedBy,
    LocalDateTime changedAt,
    String reason
) {}