package com.example.ecommerce_order_service.clients.inventoryClient.dtos;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDto {
    private Long productId;
    private Integer totalStock;
    private Integer reservedStock;
    private Integer soldStock;
    private Integer damagedStock;
    private Integer availableStock;
}
