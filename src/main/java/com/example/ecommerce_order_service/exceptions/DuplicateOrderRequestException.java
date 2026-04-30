package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class DuplicateOrderRequestException extends RuntimeException {
    private final String idempotencyKey;
    private final String message;
    public DuplicateOrderRequestException(String key) {
        super("Duplicate order request with idempotency key: " + key);
        this.idempotencyKey = key;
        this.message = "Duplicate order request with idempotency key: " + key;
    }
}
