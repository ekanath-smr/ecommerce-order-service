package com.example.ecommerce_order_service.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequestDto {
    @NotNull
    private Long userId;
    private Long cartId;
}
