package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class OrderAlreadyDeliveredException extends RuntimeException {
    private final Long orderId;
    private final String message;
    public OrderAlreadyDeliveredException(Long orderId) {
        super("Order with id=" + orderId +  ", already delivered.");
        this.orderId = orderId;
        this.message = "Order with id=" + orderId +  ", already delivered.";
    }
}
