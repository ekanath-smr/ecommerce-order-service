package com.example.ecommerce_order_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponseDto {
    private Long productId;
    private String productNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private Integer quantity;
    private BigDecimal subTotal;
}
