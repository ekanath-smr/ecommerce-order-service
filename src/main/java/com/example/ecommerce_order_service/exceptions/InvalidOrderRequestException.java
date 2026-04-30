package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class InvalidOrderRequestException extends RuntimeException {
    private final Long orderId;
    private final String message;
    public InvalidOrderRequestException(String message, Long orderId) {
        super(message);
        this.orderId = orderId;
        this.message = message;
    }
    public InvalidOrderRequestException(String message) {
        super(message);
        this.orderId = null;
        this.message = message;
    }
}
