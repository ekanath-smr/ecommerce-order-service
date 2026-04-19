package com.example.ecommerce_order_service.services;

import com.example.ecommerce_order_service.dtos.request.CreateOrderRequestDto;
import com.example.ecommerce_order_service.dtos.response.OrderResponseDto;
import com.example.ecommerce_order_service.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class OrderServiceImpl implements OrderService {

    @Override
    public OrderResponseDto createOrder(CreateOrderRequestDto request, String idempotencyKey) {
        return null;
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        return null;
    }

    @Override
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public OrderResponseDto getOrderByIdempotencyKey(String key) {
        return null;
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {
        return null;
    }

    @Override
    public OrderResponseDto cancelOrder(Long orderId) {
        return null;
    }
}
