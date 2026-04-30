package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class InventoryServiceException extends RuntimeException {
    private final String message;
    public InventoryServiceException(String message, Throwable cause) {
        super(message);
        this.message = message;
    }
}
