package com.example.ecommerce_order_service.exceptions;

import com.example.ecommerce_order_service.models.OrderStatus;
import lombok.Getter;

@Getter
public class OrderStatusTransitionException extends RuntimeException {
    private final String message;
    public OrderStatusTransitionException(OrderStatus current, OrderStatus next) {
        super(String.format("Cannot change state from %s to %s", current, next));
        this.message = String.format("Cannot change state from %s to %s", current, next);
    }
}
