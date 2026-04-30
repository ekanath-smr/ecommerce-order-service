package com.example.ecommerce_order_service.clients.inventoryClient.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAvailabilityResponseDto {
    private Long productId;
    private Integer availableStock;
}
