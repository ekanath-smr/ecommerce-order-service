package com.example.ecommerce_order_service.mappers;

import com.example.ecommerce_order_service.dtos.response.OrderItemResponseDto;
import com.example.ecommerce_order_service.dtos.response.OrderResponseDto;
import com.example.ecommerce_order_service.models.Order;
import com.example.ecommerce_order_service.models.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDto mapOrderToOrderResponseDto(Order order) {
        if (order == null) return null;
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .userId(order.getUserId())
                .status(order.getStatus().toString())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItemsToOrderItemResponseDtos(order.getItems()))
                .build();
    }

    private static List<OrderItemResponseDto> mapOrderItemsToOrderItemResponseDtos(List<OrderItem> items) {
        if (items == null) return List.of();

        return items.stream()
                .map(OrderMapper::mapOrderItemToOrderItemResponseDto)
                .collect(Collectors.toList());
    }

    private static OrderItemResponseDto mapOrderItemToOrderItemResponseDto(OrderItem item) {
        return OrderItemResponseDto.builder()
                .productId(item.getProductId())
                .productNameSnapshot(item.getProductNameSnapshot())
                .unitPriceSnapshot(item.getUnitPriceSnapshot())
                .quantity(item.getQuantity())
                .subTotal(
                        item.getUnitPriceSnapshot()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                ).build();
    }

    public static OrderItem toOrderItemEntity(Long productId, String productName, BigDecimal price, Integer quantity) {
        return OrderItem.builder()
                .productId(productId)
                .productNameSnapshot(productName)
                .unitPriceSnapshot(price)
                .quantity(quantity)
                .build();
    }
}
