package com.example.ecommerce_order_service.services;

import com.example.ecommerce_order_service.dtos.request.CreateOrderRequestDto;
import com.example.ecommerce_order_service.dtos.response.OrderResponseDto;
import com.example.ecommerce_order_service.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(CreateOrderRequestDto request, String idempotencyKey);
    OrderResponseDto getOrderById(Long orderId, Long userId);
    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);
    OrderResponseDto getOrderByIdempotencyKey(String key, Long userId);
    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status, Long userId);
    OrderResponseDto cancelOrder(Long orderId, Long userId);
}
