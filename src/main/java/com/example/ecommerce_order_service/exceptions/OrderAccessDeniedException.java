package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class OrderAccessDeniedException extends RuntimeException {
    private final Long orderId;
    private final String message;
    public OrderAccessDeniedException(Long orderId) {
        super("Access denied for order: " + orderId);
        this.orderId = orderId;
        this.message = "Access denied for order: " + orderId;
    }
    public OrderAccessDeniedException(String message) {
        super(message);
        this.orderId = null;
        this.message = message;
    }
}
