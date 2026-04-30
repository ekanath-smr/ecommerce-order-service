package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {
    private final Long orderId;
    private final String message;
    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
        this.orderId = orderId;
        this.message = "Order not found with id: " + orderId;
    }
    public OrderNotFoundException(String key) {
        super("Order not found for key: " + key);
        this.orderId = null;
        this.message = "Order not found for key: " + key;
    }
}
