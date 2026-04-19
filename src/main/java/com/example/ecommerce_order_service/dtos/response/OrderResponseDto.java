package com.example.ecommerce_order_service.dtos.response;


import com.example.ecommerce_order_service.models.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items;
}
