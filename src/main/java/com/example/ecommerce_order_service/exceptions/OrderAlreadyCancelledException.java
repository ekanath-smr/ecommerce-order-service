package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class OrderAlreadyCancelledException extends RuntimeException {
    private final Long orderId;
    private final String message;
    public OrderAlreadyCancelledException(Long orderId) {
        super("Order already cancelled: " + orderId);
        this.orderId = orderId;
        this.message = "Order already cancelled: " + orderId;
    }
}
