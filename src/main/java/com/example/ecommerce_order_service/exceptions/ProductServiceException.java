package com.example.ecommerce_order_service.exceptions;

import lombok.Getter;

@Getter
public class ProductServiceException extends RuntimeException {
    private final String message;
    public ProductServiceException(String message, Throwable cause) {
        super(message);
        this.message = message;
    }
}
